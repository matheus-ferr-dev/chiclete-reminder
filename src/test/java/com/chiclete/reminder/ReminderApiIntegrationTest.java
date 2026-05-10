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
              "MEDIA"
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
                "BAIXA"
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
                "ALTA"
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
    void grupos_adicionar_membro() throws Exception {
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmails", hasSize(2)));

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Família"));
    }
}
