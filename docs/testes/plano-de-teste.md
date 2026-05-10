# Plano de teste (resumo)

## Níveis

1. **Unitário (domínio):** getters/setters e invariantes simples da entidade `Reminder` — `ReminderApplicationTests`.
2. **Integração (API):** `ReminderApiIntegrationTest` com MockMvc, H2 em memória e transações revertidas.
3. **BDD:** Cucumber (`src/test/resources/features/lembretes.feature`) exercitando cadastro e criação de lembrete via HTTP real (`RANDOM_PORT`).

## Casos de integração cobertos

- Cadastro + criação + listagem de lembrete.
- Modo Chiclete: três ignorados com limite 3 elevam prioridade de BAIXA para MEDIA.
- Compartilhamento: convidado vê lembrete na própria lista.
- Grupos: criador adiciona segundo membro; convidado lista o grupo.

## Execução

```bash
./mvnw test
```

Relatório HTML opcional do Cucumber em `target/cucumber-report.html` (configurado no Surefire).
