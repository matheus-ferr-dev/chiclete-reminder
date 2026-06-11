# Plano de teste (resumo)

## Níveis

1. **Unitário (domínio):** getters/setters e invariantes simples da entidade `Reminder` — `ReminderApplicationTests`.
2. **Integração (API):** `ReminderApiIntegrationTest` com MockMvc, H2 em memória e transações revertidas.
3. **BDD:** Cucumber em [`cenarios-bdd.feature`](../../cenarios-bdd.feature) (raiz do repositório), exercitando fluxos essenciais via HTTP real (`RANDOM_PORT` + `TestRestTemplate`).

## Cenários BDD (Cucumber)

Arquivo único: **`cenarios-bdd.feature`** (raiz do projeto).

| Funcionalidade | Cenário |
|----------------|---------|
| Cadastro e lembretes | Fluxo mínimo de API — cadastro + criar lembrete |
| Autenticação | Login com credenciais válidas |
| Autenticação | Login com senha incorreta |
| Modo Chiclete | Prioridade escala após ignorar notificações (3× ignore → BAIXA → MEDIA) |
| Compartilhamento | Convidado vê lembrete compartilhado |
| Grupos | Membro convidado visualiza o grupo |
| Conclusão | Marcar lembrete como concluído |

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
