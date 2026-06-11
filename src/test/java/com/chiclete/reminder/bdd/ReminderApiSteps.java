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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReminderApiSteps {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, String> tokensPorEmail = new HashMap<>();
    private final Map<String, Long> lembreteIdPorTitulo = new HashMap<>();
    private final Map<String, Long> grupoIdPorNome = new HashMap<>();

    private String usuarioAtual;
    private ResponseEntity<String> lastResponse;

    private String tokenAtual() {
        return tokensPorEmail.get(usuarioAtual);
    }

    private HttpHeaders jsonHeaders(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (email != null) {
            headers.setBearerAuth(tokensPorEmail.get(email));
        }
        return headers;
    }

    private void registrarUsuario(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("name", "Usuário BDD", "email", email, "password", senha)
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register",
                new HttpEntity<>(body, jsonHeaders(null)),
                String.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode node = objectMapper.readTree(response.getBody());
        tokensPorEmail.put(email, node.get("token").asText());
    }

    @Given("a API está disponível")
    public void apiDisponivel() {
        ResponseEntity<String> health = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Given("que estou autenticado como {string}")
    public void autenticadoComo(String email) throws Exception {
        registrarUsuario(email, "senha123");
        usuarioAtual = email;
    }

    @Given("que existe um usuário cadastrado com email {string} e senha {string}")
    public void usuarioCadastrado(String email, String senha) throws Exception {
        registrarUsuario(email, senha);
    }

    @Given("que {string} criou o lembrete {string}")
    public void usuarioCriouLembrete(String email, String titulo) throws Exception {
        if (!tokensPorEmail.containsKey(email)) {
            registrarUsuario(email, "senha123");
        }
        usuarioAtual = email;
        crioLembrete(titulo, "ALTA");
    }

    @Given("que {string} criou o grupo {string}")
    public void usuarioCriouGrupo(String email, String nome) throws Exception {
        if (!tokensPorEmail.containsKey(email)) {
            registrarUsuario(email, "senha123");
        }
        String body = objectMapper.writeValueAsString(Map.of("name", nome));
        lastResponse = restTemplate.postForEntity(
                "/api/groups",
                new HttpEntity<>(body, jsonHeaders(email)),
                String.class
        );
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(201);
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        grupoIdPorNome.put(nome, node.get("id").asLong());
    }

    @Given("{string} adicionou {string} ao grupo {string}")
    public void usuarioAdicionouMembro(String ownerEmail, String memberEmail, String grupoNome) throws Exception {
        if (!tokensPorEmail.containsKey(memberEmail)) {
            registrarUsuario(memberEmail, "senha123");
        }
        long groupId = grupoIdPorNome.get(grupoNome);
        String body = objectMapper.writeValueAsString(Map.of("email", memberEmail));
        lastResponse = restTemplate.postForEntity(
                "/api/groups/" + groupId + "/members",
                new HttpEntity<>(body, jsonHeaders(ownerEmail)),
                String.class
        );
        assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Given("{string} compartilhou o lembrete {string} com {string}")
    public void usuarioCompartilhouLembrete(String ownerEmail, String titulo, String guestEmail) throws Exception {
        if (!tokensPorEmail.containsKey(guestEmail)) {
            registrarUsuario(guestEmail, "senha123");
        }
        long id = lembreteIdPorTitulo.get(titulo);
        String body = objectMapper.writeValueAsString(Map.of("email", guestEmail));
        lastResponse = restTemplate.postForEntity(
                "/api/reminders/" + id + "/share",
                new HttpEntity<>(body, jsonHeaders(ownerEmail)),
                String.class
        );
        assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @When("me cadastro com email {string} e senha {string}")
    public void cadastro(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("name", "Usuário BDD", "email", email, "password", senha)
        );
        lastResponse = restTemplate.postForEntity(
                "/api/auth/register",
                new HttpEntity<>(body, jsonHeaders(null)),
                String.class
        );
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        tokensPorEmail.put(email, node.get("token").asText());
        usuarioAtual = email;
    }

    @When("faço login com email {string} e senha {string}")
    public void login(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", senha));
        lastResponse = restTemplate.postForEntity(
                "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders(null)),
                String.class
        );
        if (lastResponse.getStatusCode().is2xxSuccessful()) {
            JsonNode node = objectMapper.readTree(lastResponse.getBody());
            tokensPorEmail.put(email, node.get("token").asText());
            usuarioAtual = email;
        }
    }

    @When("crio um lembrete {string} com prioridade {string}")
    public void crioLembrete(String titulo, String prioridade) throws Exception {
        criarLembrete(titulo, prioridade, false);
    }

    @When("crio um lembrete {string} com modo chiclete ativo e prioridade {string}")
    public void crioLembreteChiclete(String titulo, String prioridade) throws Exception {
        criarLembrete(titulo, prioridade, true);
    }

    private void criarLembrete(String titulo, String prioridade, boolean chewing) throws Exception {
        String scheduled = ISO.format(LocalDateTime.of(2026, 8, 15, 10, 0));
        String body = """
                {"title":"%s","description":null,"scheduledAt":"%s","chewing":%s,"intervalMinutes":%s,"priority":"%s"}
                """.formatted(titulo, scheduled, chewing, chewing ? 15 : "null", prioridade);

        lastResponse = restTemplate.postForEntity(
                "/api/reminders",
                new HttpEntity<>(body, jsonHeaders(usuarioAtual)),
                String.class
        );
        if (lastResponse.getStatusCode().is2xxSuccessful()) {
            JsonNode node = objectMapper.readTree(lastResponse.getBody());
            lembreteIdPorTitulo.put(titulo, node.get("id").asLong());
        }
    }

    @When("ignoro o lembrete chiclete {string} {int} vezes")
    public void ignoroLembreteChiclete(String titulo, int vezes) {
        long id = lembreteIdPorTitulo.get(titulo);
        for (int i = 0; i < vezes; i++) {
            lastResponse = restTemplate.postForEntity(
                    "/api/reminders/" + id + "/chewing/ignore",
                    new HttpEntity<>(null, jsonHeaders(usuarioAtual)),
                    String.class
            );
        }
    }

    @When("{string} lista seus lembretes")
    public void listaLembretes(String email) {
        usuarioAtual = email;
        lastResponse = restTemplate.exchange(
                "/api/reminders",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(email)),
                String.class
        );
    }

    @When("listo meus lembretes")
    public void listoMeusLembretes() {
        lastResponse = restTemplate.exchange(
                "/api/reminders",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(usuarioAtual)),
                String.class
        );
    }

    @When("{string} lista seus grupos")
    public void listaGrupos(String email) {
        lastResponse = restTemplate.exchange(
                "/api/groups",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(email)),
                String.class
        );
    }

    @When("marco o lembrete {string} como concluído")
    public void marcoConcluido(String titulo) {
        long id = lembreteIdPorTitulo.get(titulo);
        String body = "{\"completed\":true}";
        lastResponse = restTemplate.exchange(
                "/api/reminders/" + id + "/complete",
                HttpMethod.PATCH,
                new HttpEntity<>(body, jsonHeaders(usuarioAtual)),
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

    @Then("o login retorna {int}")
    public void loginStatus(int expected) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expected);
    }

    @Then("a resposta contém um token JWT")
    public void respostaContemToken() throws Exception {
        JsonNode node = objectMapper.readTree(lastResponse.getBody());
        assertThat(node.has("token")).isTrue();
        assertThat(node.get("token").asText()).isNotBlank();
    }

    @Then("o lembrete {string} tem ignoreCount {int}")
    public void lembreteIgnoreCount(String titulo, int expected) throws Exception {
        long id = lembreteIdPorTitulo.get(titulo);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reminders/" + id,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(usuarioAtual)),
                String.class
        );
        JsonNode node = objectMapper.readTree(response.getBody());
        assertThat(node.get("ignoreCount").asInt()).isEqualTo(expected);
    }

    @Then("a prioridade do lembrete {string} é {string}")
    public void prioridadeLembrete(String titulo, String prioridade) throws Exception {
        long id = lembreteIdPorTitulo.get(titulo);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reminders/" + id,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(usuarioAtual)),
                String.class
        );
        JsonNode node = objectMapper.readTree(response.getBody());
        assertThat(node.get("priority").asText()).isEqualTo(prioridade);
    }

    @Then("vê {int} lembrete com título {string}")
    public void vejoLembretesComTitulo(int quantidade, String titulo) throws Exception {
        assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode lista = objectMapper.readTree(lastResponse.getBody());
        assertThat(lista).hasSize(quantidade);
        assertThat(lista.get(0).get("title").asText()).isEqualTo(titulo);
    }

    @Then("vê o grupo {string}")
    public void vejoGrupo(String nome) throws Exception {
        assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode lista = objectMapper.readTree(lastResponse.getBody());
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).get("name").asText()).isEqualTo(nome);
    }

    @Then("o lembrete {string} está concluído")
    public void lembreteConcluido(String titulo) throws Exception {
        long id = lembreteIdPorTitulo.get(titulo);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reminders/" + id,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders(usuarioAtual)),
                String.class
        );
        JsonNode node = objectMapper.readTree(response.getBody());
        assertThat(node.get("completed").asBoolean()).isTrue();
    }
}
