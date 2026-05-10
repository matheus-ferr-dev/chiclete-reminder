# Arquitetura do Sistema — Chiclete Reminder

## 1. Visão Geral

O Chiclete Reminder é uma API REST desenvolvida em Java com Spring Boot,
seguindo arquitetura em camadas. O sistema gerencia lembretes pessoais e
compartilhados, com suporte ao Modo Chiclete — notificações persistentes
até a conclusão da tarefa.

---

## 2. Decisões Técnicas

| Decisão | Escolha | Justificativa |
|--------|---------|---------------|
| Linguagem | Java 21 | Tipagem forte, ecossistema maduro, sugerido pela disciplina |
| Framework | Spring Boot 3.5 | Padrão de mercado para APIs REST em Java |
| Banco de dados | PostgreSQL | Relacional, robusto, open source |
| ORM | JPA / Hibernate | Integração nativa com Spring, reduz SQL manual |
| Migrations | Flyway | Controle de versão do banco, integrado ao Spring |
| Containerização | Docker + Docker Compose | Ambiente padronizado para todo o grupo |
| Testes unitários | JUnit 5 | Padrão do ecossistema Java |
| BDD | Cucumber | Testes em linguagem natural (Gherkin) |
| Build | Maven | Gerenciamento de dependências e build |

---

## 3. Arquitetura em Camadas

O projeto segue o padrão de camadas:
```
com.chiclete.reminder/
├── domain/    → Entidades e regras de negócio puras
├── service/   → Casos de uso, orquestração das regras
├── infra/     → Repositórios Spring Data JPA
├── ui/        → Controllers REST e tratamento de erros HTTP
└── config/    → Segurança, JWT, filtros
```

### Responsabilidade de cada camada

**domain** — contém as entidades do sistema (`Reminder`, `User`, `Group`).
Não depende de nenhuma outra camada. É o núcleo do sistema.

**service** — contém as regras de negócio e casos de uso (ex: ativar Modo
Chiclete, incrementar ignoreCount, elevar prioridade). Usa **domain** e **infra** (repositórios).

**infra** — repositórios JPA (`UserRepository`, `ReminderRepository`, `GroupRepository`).

**ui** — controllers em `/api` e `GlobalExceptionHandler`.

**config** — `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`.

---

## 4. Segurança

- Autenticação **stateless** com **JWT** (`Authorization: Bearer <token>`).
- Senhas com **BCrypt**.
- Endpoints públicos: `POST /api/auth/register`, `POST /api/auth/login`, `GET /actuator/health`.

---

## 5. Entidades Principais

### Reminder
Representa um lembrete. Campos principais:
- `title`, `description`, `scheduledAt`, `priority`
- `chewing` — Modo Chiclete ativo/inativo
- `intervalMinutes` — intervalo em minutos (uso pelo cliente agendador / notificações)
- `ignoreCount` — contador de vezes ignorado
- `completed` — se foi concluído
- `owner` — usuário dono (obrigatório)
- `sharedWith` — usuários com acesso ao lembrete (tabela `reminder_shares`)

### User
Representa um usuário do sistema. Campos principais:
- `name`, `email`, `password`
- `role` — papel do usuário: `COMUM`, `REMETENTE` ou `ADMIN`

### Group
Representa um grupo de usuários. Campos principais:
- `name`
- `members` — lista de usuários (relação ManyToMany com User)

---

## 6. Fluxo de uma Requisição
```
HTTP Request
    └── ui (Controller)
            └── service (Caso de uso)
                    └── domain (Regra de negócio)
                    └── infra (Repositório → PostgreSQL)
```

---

## 7. Ambiente de Desenvolvimento

O banco de dados roda via Docker Compose. As migrations são gerenciadas
pelo Flyway e executadas automaticamente ao subir a aplicação.

---

## 8. Regra de Negócio Principal — Modo Chiclete

Quando `chewing = true`:
1. O cliente ou integração futura dispara notificações conforme `intervalMinutes`
2. O endpoint `POST /api/reminders/{id}/chewing/ignore` incrementa `ignoreCount` (simula notificação ignorada)
3. Quando `ignoreCount` é múltiplo do limite configurado (`app.chewing.ignore-threshold`), a `priority` sobe um degrau (BAIXA → MEDIA → ALTA → URGENTE)
4. O ciclo pára quando `completed = true` (e `ignoreCount` é zerado na conclusão)