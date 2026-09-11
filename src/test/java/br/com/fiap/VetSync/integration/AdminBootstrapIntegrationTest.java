package br.com.fiap.VetSync.integration;

import br.com.fiap.VetSync.controller.AdminController;
import br.com.fiap.VetSync.controller.AuthController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminBootstrapIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BOOTSTRAP_KEY = "boot-secret-test-key-12345";

    @Test
    @DisplayName("Bootstrap único de Admin, login com senha gerada, e bloqueio de segundo bootstrap")
    void fluxoAdminBootstrap() throws Exception {
        // 1. Falha com chave errada -> 403 Forbidden
        var reqErrada = new AdminController.AdminBootstrapRequest("Admin Master", "master@vetsync.com", "chave-errada");
        mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqErrada)))
                .andExpect(status().isForbidden());

        // 2. Sucesso com chave correta -> 201 Created
        var reqCorreta = new AdminController.AdminBootstrapRequest("Admin Master", "master@vetsync.com", BOOTSTRAP_KEY);
        MvcResult res = mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqCorreta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAdmin").isNotEmpty())
                .andExpect(jsonPath("$.senhaTemporaria").isNotEmpty())
                .andReturn();

        JsonNode adminNode = objectMapper.readTree(res.getResponse().getContentAsString());
        String senhaTemporaria = adminNode.get("senhaTemporaria").asText();
        assertThat(senhaTemporaria).isNotBlank();

        // 3. Segunda tentativa de bootstrap deve ser bloqueada -> 409 Conflict
        var reqSegunda = new AdminController.AdminBootstrapRequest("Outro Admin", "outro@vetsync.com", BOOTSTRAP_KEY);
        mockMvc.perform(post("/admins/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqSegunda)))
                .andExpect(status().isConflict());

        // 4. Admin loga no sistema com a senha temporária gerada
        var loginAdmin = new AuthController.LoginRequest("master@vetsync.com", senhaTemporaria);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
