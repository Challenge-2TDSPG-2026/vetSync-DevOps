package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.EventoSaude;
import br.com.fiap.VetSync.entity.StatusEvento;
import br.com.fiap.VetSync.security.PerfilUtils;
import br.com.fiap.VetSync.service.EventoService;
import br.com.fiap.VetSync.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos de Saúde", description = "Fluxo agendado → concluído/cancelado. Tutor escolhe veterinário, data e horário; o único requisito é que o veterinário não tenha bloqueio nem outro evento agendado nesse mesmo horário.")
public class EventoController {

    private final EventoService eventoService;
    private final PetService petService;

    public record EventoAgendarRequest(
            @NotNull(message = "idPet é obrigatório") Long idPet,
            @NotNull(message = "idTipoEvento é obrigatório") Long idTipoEvento,
            @NotNull(message = "idVeterinario é obrigatório") Long idVeterinario,
            @NotNull(message = "dtEvento é obrigatória") LocalDate dtEvento,
            @NotBlank(message = "hrEvento é obrigatória")
            @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "hrEvento deve estar no formato HH:mm, ex: 14:30")
            String hrEvento,
            String dsObservacao
    ) {}

    public record EventoConcluirRequest(
            String dsObservacao,
            @PositiveOrZero(message = "vlCusto não pode ser negativo") BigDecimal vlCusto
    ) {}

    public record EventoCancelarRequest(
            @NotBlank(message = "motivo do cancelamento é obrigatório") String motivo,
            @FutureOrPresent(message = "reagendarPara não pode ser uma data passada") LocalDate reagendarPara,
            @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "horaReagendarPara deve estar no formato HH:mm, ex: 14:30")
            String horaReagendarPara
    ) {}

    public record EventoCancelarResponse(EventoResponse eventoCancelado, EventoResponse novoEvento) {}

    public record EventoResponse(
            Long idEvento,
            String status,
            String nmTipoEvento,
            String dsCategoria,
            String nmVeterinario,
            LocalDate dtEvento,
            String hrEvento,
            String dsObservacao,
            String motivoCancelamento,
            BigDecimal vlCusto,
            Long idPet
    ) {}

    private EventoResponse toResponse(EventoSaude evento) {
        return new EventoResponse(
                evento.getIdEvento(),
                evento.getDsStatus().name(),
                evento.getTipoEvento() != null ? evento.getTipoEvento().getNmTipoEvento() : null,
                evento.getTipoEvento() != null ? evento.getTipoEvento().getDsCategoria() : null,
                evento.getVeterinario() != null ? evento.getVeterinario().getNmVeterinario() : null,
                evento.getDtEvento(),
                evento.getHrEvento(),
                evento.getDsObservacao(),
                evento.getDsMotivoCancelamento(),
                evento.getVlCusto(),
                evento.getPet() != null ? evento.getPet().getIdPet() : null
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Tutor agenda um evento de saúde para um pet dele, escolhendo veterinário e horário livre na agenda dele")
    public EventoResponse agendar(Authentication authentication, @Valid @RequestBody EventoAgendarRequest request) {
        boolean donoDoPet = petService.buscarPorId(request.idPet()).getTutor().getDsEmail()
                .equalsIgnoreCase(authentication.getName());
        if (!donoDoPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse pet não pertence a você");
        }
        EventoSaude evento = EventoSaude.builder()
                .dtEvento(request.dtEvento())
                .hrEvento(request.hrEvento())
                .dsObservacao(request.dsObservacao())
                .build();
        return toResponse(eventoService.agendar(evento, request.idPet(), request.idTipoEvento(), request.idVeterinario()));
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Tutor vê os eventos dos próprios pets; veterinário vê os eventos atribuídos a ele.")
    public List<EventoResponse> listar(Authentication authentication) {
        List<EventoSaude> eventos = PerfilUtils.isVeterinario(authentication)
                ? eventoService.listarParaVeterinario(authentication.getName())
                : eventoService.listarParaTutor(authentication.getName());
        return eventos.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@eventoSecurity.isRelacionado(#id, authentication)")
    @Operation(summary = "Buscar evento por ID (só tutor dono ou veterinário responsável)")
    public EventoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(eventoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasRole('VETERINARIO') and @eventoSecurity.isVeterinarioResponsavel(#id, authentication)")
    @Operation(summary = "Veterinário conclui um evento AGENDADO, com observações clínicas e custo final")
    public EventoResponse concluir(@PathVariable Long id, @Valid @RequestBody EventoConcluirRequest request) {
        return toResponse(eventoService.concluir(id, request.dsObservacao(), request.vlCusto()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('TUTOR') and @eventoSecurity.isTutorDoPet(#id, authentication)")
    @Operation(summary = "Tutor cancela um evento AGENDADO", description = "Motivo é obrigatório. Veterinário não cancela mais consultas. Se 'reagendarPara' vier preenchido, já cria um novo evento AGENDADO na nova data, no mesmo pet/tipo/veterinário.")
    public EventoCancelarResponse cancelar(@PathVariable Long id, @Valid @RequestBody EventoCancelarRequest request) {
        EventoService.ResultadoCancelamento resultado = eventoService.cancelar(id, request.motivo(), request.reagendarPara(), request.horaReagendarPara());
        return new EventoCancelarResponse(
                toResponse(resultado.eventoCancelado()),
                resultado.novoEvento() != null ? toResponse(resultado.novoEvento()) : null
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@eventoSecurity.canDelete(#id, authentication)")
    @Operation(summary = "Remover evento", description = "Veterinário sempre pode; tutor só enquanto o evento ainda está AGENDADO.")
    public void deletar(@PathVariable Long id) {
        eventoService.deletar(id);
    }

    @GetMapping("/pet/{idPet}/gasto-total")
    @Operation(summary = "Somar o gasto total (só eventos CONCLUIDOS) de um pet")
    public BigDecimal gastoTotal(@PathVariable Long idPet) {
        return eventoService.calcularGastoTotal(idPet);
    }

    @GetMapping("/pet/{idPet}/alertas")
    @Operation(summary = "Histórico + alerta de atraso por tipo de evento (só considera eventos CONCLUIDOS)")
    public List<EventoService.AlertaEvento> alertas(@PathVariable Long idPet) {
        return eventoService.gerarAlertas(idPet);
    }
}