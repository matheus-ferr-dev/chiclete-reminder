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

O projeto segue o padrão de 4 camadas:
```
com.chiclete.reminder/
├── domain/    → Entidades e regras de negócio puras
├── service/   → Casos de uso, orquestração das regras
├── infra/     → Repositórios, acesso ao banco, configurações
└── ui/        → Controllers REST, entrada e saída de dados
```

### Responsabilidade de cada camada

**domain** — contém as entidades do sistema (`Reminder`, `User`, `Group`).
Não depende de nenhuma outra camada. É o núcleo do sistema.

**service** — contém as regras de negócio e casos de uso (ex: ativar Modo
Chiclete, incrementar ignoreCount, elevar prioridade). Depende apenas do domain.

**infra** — contém os repositórios JPA que fazem acesso ao banco.
Implementa as interfaces definidas pelo service.

**ui** — contém os controllers REST que recebem as requisições HTTP e
delegam para o service. É a única camada exposta externamente.

---

## 4. Entidades Principais

### Reminder
Representa um lembrete. Campos principais:
- `title`, `description`, `scheduledAt`, `priority`
- `chewing` — Modo Chiclete ativo/inativo
- `intervalMinutes` — intervalo de repetição em minutos
- `ignoreCount` — contador de vezes ignorado
- `completed` — se foi concluído

### User
Representa um usuário do sistema. Campos principais:
- `name`, `email`, `password`
- `role` — papel do usuário: `COMUM`, `REMETENTE` ou `ADMIN`

### Group
Representa um grupo de usuários. Campos principais:
- `name`
- `members` — lista de usuários (relação ManyToMany com User)

---

## 5. Fluxo de uma Requisição
```
HTTP Request
    └── ui (Controller)
            └── service (Caso de uso)
                    └── domain (Regra de negócio)
                    └── infra (Repositório → PostgreSQL)
```

---

## 6. Ambiente de Desenvolvimento

O banco de dados roda via Docker Compose. As migrations são gerenciadas
pelo Flyway e executadas automaticamente ao subir a aplicação.

---

## 7. Regra de Negócio Principal — Modo Chiclete

Quando `chewing = true`:
1. O sistema envia notificações no intervalo definido em `intervalMinutes`
2. A cada notificação ignorada, `ignoreCount` é incrementado
3. Se `ignoreCount` atingir o limite configurado, a `priority` é elevada automaticamente
4. O lembrete só para quando `completed = true`