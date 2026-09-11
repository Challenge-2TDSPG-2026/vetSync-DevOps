package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Admin;
import br.com.fiap.VetSync.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("POST /admins/bootstrap - Sucesso (Público)")
    void bootstrap_Sucesso() throws Exception {
        var req = new AdminController.AdminBootstrapRequest("Admin Master", "master@vetsync.com", "chave-secreta");
        Admin admin = Admin.builder().idAdmin(1L).nmAdmin("Admin Master").dsEmail("master@vetsync.com").build();
        when(adminService.bootstrap(eq("Admin Master"), eq("master@vetsync.com"), eq("chave-secreta")))
                .thenReturn(new AdminService.NovoAdmin(admin, "TempPass123"));

        mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").value(1))
                .andExpect(jsonPath("$.nome").value("Admin Master"))
                .andExpect(jsonPath("$.email").value("master@vetsync.com"))
                .andExpect(jsonPath("$.senhaTemporaria").value("TempPass123"));
    }

    @Test
    @DisplayName("POST /admins/bootstrap - Falha validação (campos vazios) -> 400 Bad Request")
    void bootstrap_ValidacaoInvalida() throws Exception {
        var req = new AdminController.AdminBootstrapRequest("", "email-invalido", "");

        mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos").isMap());
    }

    @Test
    @DisplayName("POST /admins/bootstrap - Chave inválida -> 403 Forbidden")
    void bootstrap_ChaveInvalida() throws Exception {
        var req = new AdminController.AdminBootstrapRequest("Admin", "admin@vetsync.com", "errada");
        when(adminService.bootstrap(eq("Admin"), eq("admin@vetsync.com"), eq("errada")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Chave de bootstrap inválida"));

        mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("Chave de bootstrap inválida"));
    }

    @Test
    @DisplayName("POST /admins - Sucesso quando autenticado como ROLE_ADMIN")
    @WithMockUser(roles = "ADMIN")
    void criar_Sucesso() throws Exception {
        var req = new AdminController.AdminRequest("Admin Dois", "dois@vetsync.com");
        Admin admin = Admin.builder().idAdmin(2L).nmAdmin("Admin Dois").dsEmail("dois@vetsync.com").build();
        when(adminService.cadastrar("Admin Dois", "dois@vetsync.com"))
                .thenReturn(new AdminService.NovoAdmin(admin, "TempPass456"));

        mockMvc.perform(post("/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").value(2))
                .andExpect(jsonPath("$.nome").value("Admin Dois"));
    }

    @Test
    @DisplayName("POST /admins - Falha quando usuário não tem ROLE_ADMIN -> 403 Forbidden")
    @WithMockUser(roles = "TUTOR")
    void criar_AcessoNegadoParaTutor() throws Exception {
        var req = new AdminController.AdminRequest("Admin Três", "tres@vetsync.com");

        mockMvc.perform(post("/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
