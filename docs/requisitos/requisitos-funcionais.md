# Requisitos funcionais — Chiclete

| ID | Requisito | Cobertura no sistema |
|----|-----------|----------------------|
| RF01 | Cadastro de usuário | `POST /api/auth/register` |
| RF02 | Login com JWT | `POST /api/auth/login` |
| RF03 | CRUD de lembretes (criar, listar, obter, atualizar, excluir) | `/api/reminders` |
| RF04 | Marcar lembrete como concluído | `PATCH /api/reminders/{id}/complete` |
| RF05 | Ativar/desativar Modo Chiclete | `PATCH /api/reminders/{id}/chewing` |
| RF06 | Simular notificação ignorada e escalar prioridade | `POST /api/reminders/{id}/chewing/ignore` |
| RF07 | Compartilhar lembrete com outro usuário (e-mail) | `POST /api/reminders/{id}/share` |
| RF08 | Criar grupo e listar grupos do usuário | `POST /api/groups`, `GET /api/groups` |
| RF09 | Adicionar membro ao grupo | `POST /api/groups/{id}/members` |

## Regras — Modo Chiclete

- Com `chewing = true`, o cliente (ou integração futura de push) pode registrar ignorados via `chewing/ignore`.
- A cada `ignoreThreshold` ignorados consecutivos na contagem global do lembrete, a prioridade sobe um nível (BAIXA → MEDIA → ALTA → URGENTE), até o teto.
- Concluir o lembrete zera `ignoreCount`.

## Fora de escopo (evolução)

- Cliente Android e notificações push reais.
- Envio automático de e-mail/SMS.
