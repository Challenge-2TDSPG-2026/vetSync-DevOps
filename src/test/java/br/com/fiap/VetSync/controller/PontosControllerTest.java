package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.AdminRepository;
import br.com.fiap.VetSync.service.PontosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PontosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PontosService pontosService;

    @MockBean
    private AdminRepository adminRepository;

    @Test
    @DisplayName("GET /pontos - ADMIN vê lançamentos pendentes")
    @WithMockUser(roles = "ADMIN")
    void listar_Admin() throws Exception {
        LancamentoPontos lanc = LancamentoPontos.builder()
                .idLancamento(1L)
                .dsStatus(StatusLancamentoPontos.PENDENTE)
                .nrPontos(30)
                .dtLancamento(LocalDate.now())
                .build();
        when(pontosService.listarPendentes()).thenReturn(List.of(lanc));

        mockMvc.perform(get("/pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idLancamento").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].nrPontos").value(30));
    }

    @Test
    @DisplayName("GET /pontos - TUTOR vê os próprios lançamentos")
    @WithMockUser(username = "tutor@teste.com", roles = "TUTOR")
    void listar_Tutor() throws Exception {
        LancamentoPontos lanc = LancamentoPontos.builder()
                .idLancamento(2L)
                .dsStatus(StatusLancamentoPontos.LIBERADO)
                .nrPontos(15)
                .dtLancamento(LocalDate.now())
                .build();
        when(pontosService.listarParaTutor("tutor@teste.com")).thenReturn(List.of(lanc));

        mockMvc.perform(get("/pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idLancamento").value(2))
                .andExpect(jsonPath("$[0].status").value("LIBERADO"));
    }

    @Test
    @DisplayName("PATCH /pontos/{id}/liberar - ADMIN libera pontos com sucesso")
    @WithMockUser(username = "admin@teste.com", roles = "ADMIN")
    void liberar_AdminSucesso() throws Exception {
        Admin admin = Admin.builder().idAdmin(5L).dsEmail("admin@teste.com").build();
        when(adminRepository.findByDsEmail("admin@teste.com")).thenReturn(Optional.of(admin));

        LancamentoPontos liberado = LancamentoPontos.builder()
                .idLancamento(10L)
                .dsStatus(StatusLancamentoPontos.LIBERADO)
                .nrPontos(50)
                .dtLancamento(LocalDate.now())
                .build();

        when(pontosService.liberar(10L, 5L)).thenReturn(liberado);

        mockMvc.perform(patch("/pontos/10/liberar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLancamento").value(10))
                .andExpect(jsonPath("$.status").value("LIBERADO"));
    }

    @Test
    @DisplayName("PATCH /pontos/{id}/liberar - Falha 403 para TUTOR")
    @WithMockUser(roles = "TUTOR")
    void liberar_TutorNegado() throws Exception {
        mockMvc.perform(patch("/pontos/10/liberar"))
                .andExpect(status().isForbidden());
    }
}
