package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Recompensa;
import br.com.fiap.VetSync.entity.Resgate;
import br.com.fiap.VetSync.entity.TipoRecompensa;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import br.com.fiap.VetSync.service.RecompensaService;
import br.com.fiap.VetSync.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/recompensas")
@RequiredArgsConstructor
@Tag(name = "Recompensas", description = "Catálogo de prêmios, saldo de pontos e resgate")
public class RecompensaController {

    private final RecompensaService recompensaService;
    private final TutorService tutorService;
    private final VeterinarioRepository veterinarioRepository;

    public record RecompensaRequest(
            @NotBlank(message = "Nome é obrigatório")
            String nome,

            String descricao,

            @NotNull(message = "Custo em pontos é obrigatório")
            @Positive(message = "Custo em pontos deve ser positivo")
            Integer custoPontos,

            @NotNull(message = "Tipo é obrigatório")
            TipoRecompensa tipo
    ) {}

    public record RecompensaResponse(
            Long idRecompensa, String nome, String descricao, Integer custoPontos, String tipo, boolean ativa
    ) {}

    public record ResgateResponse(
            Long idResgate, String status, LocalDateTime dtResgate,
            String nmRecompensa, Integer custoPontos, String nmVeterinarioValidador
    ) {}

    public record ValidarResgateRequest(
            @NotNull(message = "Campo 'aprovado' é obrigatório")
            Boolean aprovado
    ) {}

    private RecompensaResponse toResponse(Recompensa r) {
        return new RecompensaResponse(r.getIdRecompensa(), r.getNmRecompensa(), r.getDsDescricao(),
                r.getNrCustoPontos(), r.getDsTipo().name(), Boolean.TRUE.equals(r.getFlAtivo()));
    }

    private ResgateResponse toResponse(Resgate r) {
        return new ResgateResponse(
                r.getIdResgate(), r.getDsStatus().name(), r.getDtResgate(),
                r.getRecompensa().getNmRecompensa(), r.getRecompensa().getNrCustoPontos(),
                r.getVeterinarioValidador() != null ? r.getVeterinarioValidador().getNmVeterinario() : null
        );
    }

    private Long idTutorAutenticado(Authentication authentication) {
        return tutorService.buscarPorEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutor autenticado não encontrado"))
                .getIdTutor();
    }

    private Long idVeterinarioAutenticado(Authentication authentication) {
        return veterinarioRepository.findByDsEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinário autenticado não encontrado"))
                .getIdVeterinario();
    }

    @GetMapping
    @Operation(summary = "Listar recompensas ativas do catálogo")
    public List<RecompensaResponse> listar() {
        return recompensaService.listarAtivas().stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Cadastrar recompensa no catálogo. Somente VETERINARIO (equipe da clínica).")
    public RecompensaResponse criar(@Valid @RequestBody RecompensaRequest request) {
        return toResponse(recompensaService.criar(request.nome(), request.descricao(), request.custoPontos(), request.tipo()));
    }

    @GetMapping("/saldo")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Saldo de pontos do tutor autenticado")
    public int saldo(Authentication authentication) {
        return recompensaService.calcularSaldo(idTutorAutenticado(authentication));
    }

    @PatchMapping("/{id}/resgatar")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Resgatar uma recompensa", description = "Debita do saldo do tutor autenticado e cria um resgate PENDENTE, aguardando validação de um veterinário.")
    public ResgateResponse resgatar(@PathVariable Long id, Authentication authentication) {
        return toResponse(recompensaService.solicitarResgate(idTutorAutenticado(authentication), id));
    }

    @GetMapping("/resgates")
    @Operation(summary = "Listar resgates", description = "Tutor vê os próprios; veterinário vê todos os pendentes de validação.")
    public List<ResgateResponse> listarResgates(Authentication authentication) {
        boolean ehVeterinario = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VETERINARIO"));
        List<Resgate> resgates = ehVeterinario
                ? recompensaService.listarPendentes()
                : recompensaService.listarResgatesDoTutor(idTutorAutenticado(authentication));
        return resgates.stream().map(this::toResponse).toList();
    }

    @PatchMapping("/resgates/{idResgate}/validar")
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Validar ou negar um resgate pendente. Somente VETERINARIO.")
    public ResgateResponse validar(@PathVariable Long idResgate, Authentication authentication,
                                   @Valid @RequestBody ValidarResgateRequest request) {
        return toResponse(recompensaService.validar(idResgate, idVeterinarioAutenticado(authentication), request.aprovado()));
    }
}