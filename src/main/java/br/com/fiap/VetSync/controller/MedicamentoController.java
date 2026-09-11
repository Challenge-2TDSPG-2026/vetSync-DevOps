package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Medicamento;
import br.com.fiap.VetSync.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/medicamentos")
@RequiredArgsConstructor
@Tag(name = "Medicamentos", description = "Catálogo de medicamentos usados nas prescrições")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    public record MedicamentoRequest(
            @NotBlank(message = "Nome do medicamento é obrigatório") String nmMedicamento,
            String dsPrincipio,
            BigDecimal vlPrecoRef
    ) {}

    public record MedicamentoResponse(
            Long idMedicamento, String nmMedicamento, String dsPrincipio, BigDecimal vlPrecoRef
    ) {}

    private MedicamentoResponse toResponse(Medicamento m) {
        return new MedicamentoResponse(m.getIdMedicamento(), m.getNmMedicamento(), m.getDsPrincipio(), m.getVlPrecoRef());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Cadastrar medicamento no catálogo")
    public MedicamentoResponse criar(@Valid @RequestBody MedicamentoRequest request) {
        return toResponse(medicamentoService.criar(request.nmMedicamento(), request.dsPrincipio(), request.vlPrecoRef()));
    }

    @GetMapping
    @Operation(summary = "Listar medicamentos do catálogo")
    public List<MedicamentoResponse> listar() {
        return medicamentoService.listar().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar medicamento por ID")
    public MedicamentoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(medicamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Atualizar dados de um medicamento do catálogo")
    public MedicamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody MedicamentoRequest request) {
        return toResponse(medicamentoService.atualizar(id, request.nmMedicamento(), request.dsPrincipio(), request.vlPrecoRef()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover medicamento do catálogo", description = "Só ADMIN — evita que um vet apague sem querer um medicamento já usado em prescrições.")
    public void deletar(@PathVariable Long id) {
        medicamentoService.deletar(id);
    }
}