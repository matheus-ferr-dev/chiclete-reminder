package com.chiclete.reminder.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class ReminderApiSteps {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private ResponseEntity<String> lastResponse;

    @Given("a API está disponível")
    public void apiDisponivel() {
        ResponseEntity<String> health = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @When("me cadastro com email {string} e senha {string}")
    public void cadastro(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("name", "Usuário BDD", "email", email, "password", senha)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        lastResponse = restTemplate.postForEntity(
                "/api/auth/register",
                new HttpEntity<>(body, headers),
                String.class
        );
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        token = node.get("token").asText();
    }

    @When("crio um lembrete {string} com prioridade {string}")
    public void crioLembrete(String titulo, String prioridade) throws Exception {
        String scheduled = ISO.format(LocalDateTime.of(2026, 8, 15, 10, 0));
        String body = """
                {"title":"%s","description":null,"scheduledAt":"%s","chewing":false,"intervalMinutes":null,"priority":"%s"}
                """.formatted(titulo, scheduled, prioridade);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        lastResponse = restTemplate.postForEntity(
                "/api/reminders",
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    @Then("o cadastro retorna {int}")
    public void cadastroStatus(int expected) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expected);
    }

    @Then("a criação do lembrete retorna {int}")
    public void lembreteStatus(int expected) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expected);
    }

    @Then("o lembrete criado tem título {string}")
    public void tituloLembrete(String titulo) throws Exception {
        JsonNode n = objectMapper.readTree(lastResponse.getBody());
        assertThat(n.get("title").asText()).isEqualTo(titulo);
    }
}
