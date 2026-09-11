package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Tutor;
import br.com.fiap.VetSync.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutores")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Gerenciamento de tutores (donos dos pets)")
public class TutorController {

    private final TutorService tutorService;

    public record TutorAtualizarRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 2, max = 100)
            String nmTutor,

            @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
            String nrTelefone
    ) {}

    public record TutorResponse(
            Long idTutor,
            String nmTutor,
            String dsEmail,
            String nrTelefone,
            String dsCpf
    ) {}

    private TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getIdTutor(),
                tutor.getNmTutor(),
                tutor.getDsEmail(),
                tutor.getNrTelefone(),
                tutor.getDsCpf()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIO') or @tutorSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Buscar tutor por ID", description = "Veterinário vê qualquer um; tutor só vê o próprio cadastro.")
    public TutorResponse buscarPorId(@PathVariable Long id) {
        return toResponse(tutorService.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Listar todos os tutores. Somente VETERINARIO.")
    public List<TutorResponse> listarTodos() {
        return tutorService.listarTodos().stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tutorSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Atualizar dados do tutor (nome e telefone). Só o próprio tutor.")
    public TutorResponse atualizar(@PathVariable Long id, @RequestBody @Valid TutorAtualizarRequest request) {
        Tutor tutorAtualizado = Tutor.builder()
                .nmTutor(request.nmTutor())
                .nrTelefone(request.nrTelefone())
                .build();
        return toResponse(tutorService.atualizar(id, tutorAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@tutorSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Deletar tutor. Só o próprio tutor.")
    public void deletar(@PathVariable Long id) {
        tutorService.deletar(id);
    }
}