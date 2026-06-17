package com.chiclete.reminder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chiclete.reminder.dto.RegisterRequest;
import com.chiclete.reminder.dto.ReminderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String email) throws Exception {
        var reg = new RegisterRequest("Usuário Teste", email, "senha123");
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("token").asText();
    }

    @Test
    void cadastro_criacao_listagem_lembrete() throws Exception {
      String token = registerAndGetToken("mvp@test.com");

      var reminder = new ReminderRequest(
              "Revisar capítulo",
              "Capítulo 3",
              LocalDateTime.of(2026, 6, 1, 9, 0),
              false,
              null,
              "MEDIA",
              null,
              null,
              null,
              null,
              null
      );

      mockMvc.perform(post("/api/reminders")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(reminder)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.title").value("Revisar capítulo"))
              .andExpect(jsonPath("$.priority").value("MEDIA"));

      mockMvc.perform(get("/api/reminders").header("Authorization", "Bearer " + token))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].title").value("Revisar capítulo"));
    }

    @Test
    void modo_chiclete_escalona_prioridade_apos_ignorados() throws Exception {
        String token = registerAndGetToken("chiclete@test.com");
        var reminder = new ReminderRequest(
                "Tomar remédio",
                null,
                LocalDateTime.of(2026, 6, 2, 8, 0),
                true,
                15,
                "BAIXA",
                null,
                null,
                null,
                null,
                null
        );
      String body = mockMvc.perform(post("/api/reminders")
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(reminder)))
              .andExpect(status().isCreated())
              .andReturn()
              .getResponse()
              .getContentAsString();
      long id = objectMapper.readTree(body).get("id").asLong();

      for (int i = 0; i < 3; i++) {
          mockMvc.perform(post("/api/reminders/" + id + "/chewing/ignore")
                          .header("Authorization", "Bearer " + token))
                  .andExpect(status().isOk());
      }

      mockMvc.perform(get("/api/reminders/" + id).header("Authorization", "Bearer " + token))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.ignoreCount").value(3))
              .andExpect(jsonPath("$.priority").value("MEDIA"));
    }

    @Test
    void compartilhar_lembrete_com_outro_usuario() throws Exception {
        String ownerToken = registerAndGetToken("owner@test.com");
        String otherToken = registerAndGetToken("guest@test.com");

        var reminder = new ReminderRequest(
                "Reunião",
                null,
                LocalDateTime.of(2026, 7, 1, 14, 0),
                false,
                null,
                "ALTA",
                null,
                null,
                null,
                null,
                null
        );

        String created = mockMvc.perform(post("/api/reminders")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminder)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/reminders/" + id + "/share")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"guest@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sharedWithEmails[0]").value("guest@test.com"));

        mockMvc.perform(get("/api/reminders").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Reunião"));
    }

    @Test
    void perfil_atualiza_whatsapp_e_preferencias() throws Exception {
        String token = registerAndGetToken("perfil@test.com");

        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifyInApp").value(true));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"whatsapp":"5511999887766","notifyWhatsapp":true,"name":"Victor"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsapp").value("5511999887766"))
                .andExpect(jsonPath("$.notifyWhatsapp").value(true))
                .andExpect(jsonPath("$.name").value("Victor"));
    }

    @Test
    void grupos_fluxo_convite_aceite() throws Exception {
        String a = registerAndGetToken("alfa@test.com");
        String b = registerAndGetToken("beta@test.com");

        String groupJson = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Família\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long groupId = objectMapper.readTree(groupJson).get("id").asLong();

        mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .header("Authorization", "Bearer " + a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"beta@test.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.inviteToken").isNotEmpty());

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        String invitesJson = mockMvc.perform(get("/api/groups/invites").header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].groupInviteId").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long inviteId = objectMapper.readTree(invitesJson).get(0).get("id").asLong();

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("GROUP_INVITE"))
                .andExpect(jsonPath("$[0].groupInviteId").value((int) inviteId));

        mockMvc.perform(post("/api/groups/invites/" + inviteId + "/accept")
                        .header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmails", hasSize(2)));

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Família"));
    }

    @Test
    void admin_designa_lembrete_a_membro_do_grupo() throws Exception {
        String adminToken = registerAndGetToken("admin-grp@test.com");
        String memberToken = registerAndGetToken("membro-grp@test.com");

        String groupJson = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tarefas\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long groupId = objectMapper.readTree(groupJson).get("id").asLong();

        mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"membro-grp@test.com\"}"))
                .andExpect(status().isCreated());

        String invitesJson = mockMvc.perform(get("/api/groups/invites").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long inviteId = objectMapper.readTree(invitesJson).get(0).get("id").asLong();

        mockMvc.perform(post("/api/groups/invites/" + inviteId + "/accept")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        var reminder = new ReminderRequest(
                "Comprar material",
                "Lista da reunião",
                LocalDateTime.of(2026, 8, 1, 10, 0),
                false,
                null,
                "MEDIA",
                null,
                null,
                null,
                groupId,
                "membro-grp@test.com"
        );

        mockMvc.perform(post("/api/reminders")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reminder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sharedWithEmails[0]").value("membro-grp@test.com"));

        mockMvc.perform(get("/api/reminders").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Comprar material"));
    }

    @Test
    void grupos_convite_por_token_apos_registro() throws Exception {
        String a = registerAndGetToken("owner@test.com");

        String groupJson = mockMvc.perform(post("/api/groups")
                        .header("Authorization", "Bearer " + a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Equipa\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long groupId = objectMapper.readTree(groupJson).get("id").asLong();

        String inviteJson = mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .header("Authorization", "Bearer " + a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"novo@test.com\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(inviteJson).get("inviteToken").asText();

        mockMvc.perform(get("/api/invites/token/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("Equipa"))
                .andExpect(jsonPath("$.requiresRegistration").value(true));

        String regJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Novo","email":"novo@test.com","password":"senha123","inviteToken":"%s"}
                                """.formatted(token)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String newToken = objectMapper.readTree(regJson).get("token").asText();

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Equipa"));
    }
}
