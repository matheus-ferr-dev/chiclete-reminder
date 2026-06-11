# Plano de teste (resumo)

## Níveis

1. **Unitário (domínio):** getters/setters e invariantes simples da entidade `Reminder` — `ReminderApplicationTests`.
2. **Integração (API):** `ReminderApiIntegrationTest` com MockMvc, H2 em memória e transações revertidas.
3. **BDD:** Cucumber em `src/test/resources/features/` exercitando fluxos essenciais via HTTP real (`RANDOM_PORT` + `TestRestTemplate`).

## Cenários BDD (Cucumber)

| Arquivo | Cenário |
|---------|---------|
| `lembretes.feature` | Fluxo mínimo de API — cadastro + criar lembrete |
| `autenticacao.feature` | Login com credenciais válidas |
| `autenticacao.feature` | Login com senha incorreta |
| `modo-chiclete.feature` | Prioridade escala após ignorar notificações (3× ignore → BAIXA → MEDIA) |
| `compartilhamento.feature` | Convidado vê lembrete compartilhado |
| `grupos.feature` | Membro convidado visualiza o grupo |
| `conclusao.feature` | Marcar lembrete como concluído |

Step definitions: `src/test/java/com/chiclete/reminder/bdd/ReminderApiSteps.java`

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
