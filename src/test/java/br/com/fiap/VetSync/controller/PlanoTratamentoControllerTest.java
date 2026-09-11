package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.security.PlanoTratamentoSecurity;
import br.com.fiap.VetSync.service.PlanoTratamentoService;
import br.com.fiap.VetSync.service.VeterinarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PlanoTratamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanoTratamentoService planoTratamentoService;

    @MockBean
    private VeterinarioService veterinarioService;

    @MockBean
    private PlanoTratamentoSecurity planoTratamentoSecurity;

    @Test
    @DisplayName("POST /planos - Sucesso para VETERINARIO")
    @WithMockUser(username = "vet@teste.com", roles = "VETERINARIO")
    void criar_Sucesso() throws Exception {
        Veterinario vet = Veterinario.builder().idVeterinario(2L).nmVeterinario("Dr. V").build();
        when(veterinarioService.buscarAutenticado(any())).thenReturn(vet);

        Pet pet = Pet.builder().idPet(1L).nmPet("Thor").build();
        PlanoTratamento plano = PlanoTratamento.builder()
                .idPlano(10L)
                .pet(pet)
                .veterinario(vet)
                .nrPontosBonus(30)
                .dsStatus(StatusPlanoTratamento.EM_ANDAMENTO)
                .dtCriacao(LocalDate.now())
                .build();

        when(planoTratamentoService.criar(eq(1L), eq(2L), eq(30), eq(List.of(100L, 200L)))).thenReturn(plano);
        when(planoTratamentoService.listarItens(10L)).thenReturn(List.of());

        var req = new PlanoTratamentoController.PlanoTratamentoRequest(1L, 30, List.of(100L, 200L));

        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPlano").value(10))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.nrPontosBonus").value(30));
    }

    @Test
    @DisplayName("POST /planos - Falha validação quando lista de itens está vazia -> 400 Bad Request")
    @WithMockUser(roles = "VETERINARIO")
    void criar_ItensVazios() throws Exception {
        var req = new PlanoTratamentoController.PlanoTratamentoRequest(1L, 10, List.of());

        mockMvc.perform(post("/planos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /planos/{id} - Sucesso com segurança autorizada")
    @WithMockUser(username = "tutor@teste.com", roles = "TUTOR")
    void buscarPorId_Sucesso() throws Exception {
        when(planoTratamentoSecurity.isRelacionadoAoPlano(eq(10L), any())).thenReturn(true);

        Pet pet = Pet.builder().idPet(1L).nmPet("Thor").build();
        Veterinario vet = Veterinario.builder().nmVeterinario("Dr. V").build();
        PlanoTratamento plano = PlanoTratamento.builder()
                .idPlano(10L)
                .pet(pet)
                .veterinario(vet)
                .dsStatus(StatusPlanoTratamento.EM_ANDAMENTO)
                .dtCriacao(LocalDate.now())
                .build();

        when(planoTratamentoService.buscarPorId(10L)).thenReturn(plano);
        when(planoTratamentoService.listarItens(10L)).thenReturn(List.of());

        mockMvc.perform(get("/planos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPlano").value(10))
                .andExpect(jsonPath("$.nmPet").value("Thor"));
    }

    @Test
    @DisplayName("GET /planos/{id} - 403 Forbidden quando usuário não está relacionado ao plano")
    @WithMockUser(username = "estranho@teste.com", roles = "TUTOR")
    void buscarPorId_NaoRelacionado() throws Exception {
        when(planoTratamentoSecurity.isRelacionadoAoPlano(eq(10L), any())).thenReturn(false);

        mockMvc.perform(get("/planos/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /planos/itens/{idItem}/agendar - Sucesso por TUTOR dono do item")
    @WithMockUser(username = "tutor@teste.com", roles = "TUTOR")
    void agendarItem_Sucesso() throws Exception {
        when(planoTratamentoSecurity.isTutorDoItem(eq(100L), any())).thenReturn(true);

        TipoEvento tipo = TipoEvento.builder().nmTipoEvento("Fisio").build();
        EventoSaude ev = EventoSaude.builder().idEvento(500L).dtEvento(LocalDate.now().plusDays(1)).hrEvento("14:30").build();
        PlanoItem item = PlanoItem.builder().idItem(100L).nrOrdem(1).tipoEvento(tipo).dsStatus(StatusPlanoItem.AGENDADO).evento(ev).build();

        when(planoTratamentoService.buscarItemPorId(100L)).thenReturn(item);

        var req = new PlanoTratamentoController.PlanoItemAgendarRequest(2L, LocalDate.now().plusDays(1), "14:30", "Obs");

        mockMvc.perform(patch("/planos/itens/100/agendar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItem").value(100))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.idEvento").value(500));
    }
}
