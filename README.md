# Chiclete

## 1. Nome do Projeto

**Chiclete**

## 2. Resumo do Projeto

O Chiclete é um aplicativo de lembretes pessoais e compartilhados, desenvolvido com o objetivo de ajudar usuários a não esquecerem tarefas, compromissos e atividades importantes do dia a dia. O principal diferencial do sistema é o **Modo Chiclete**, funcionalidade que mantém o lembrete ativo e envia notificações recorrentes até que o usuário confirme a conclusão da tarefa. O sistema também permite a criação de grupos e o compartilhamento de lembretes entre usuários, tornando o aplicativo útil para famílias, estudantes e equipes de trabalho.

## 3. Problema que o Projeto Resolve

Muitas pessoas ignoram notificações simples de aplicativos de lembretes e acabam esquecendo tarefas importantes. Além disso, aplicativos tradicionais não garantem que o usuário realmente visualize ou conclua a tarefa. O Chiclete busca resolver esse problema por meio de lembretes persistentes e compartilháveis, que acompanham o usuário até a conclusão da atividade.

## 4. Público-Alvo

- Estudantes
- Famílias
- Casais
- Profissionais com rotina corrida
- Pequenas equipes de trabalho

## 5. Funcionalidades do Sistema

O sistema possui as seguintes funcionalidades principais:

- Cadastro de usuário
- Login de usuário
- Criação de lembretes
- Edição de lembretes
- Exclusão de lembretes
- Listagem de lembretes
- Criação de grupos
- Compartilhamento de lembretes
- Ativação do Modo Chiclete
- Marcar lembrete como concluído

## 6. Regra de Negócio Principal

O sistema possui uma regra de negócio chamada **Modo Chiclete**.

Quando um lembrete estiver com o Modo Chiclete ativado, o sistema deverá continuar enviando notificações recorrentes até que o usuário marque o lembrete como concluído. O sistema também poderá aumentar automaticamente a prioridade do lembrete caso ele seja ignorado várias vezes.

## 7. Tecnologias Utilizadas (Stack)

| Categoria           | Tecnologia        |
| ------------------- | ----------------- |
| Mobile              | Android Java      |
| Backend             | Spring Boot       |
| Banco               | PostgreSQL        |
| ORM                 | JPA / Hibernate   |
| Migrations          | Flyway            |
| API / health        | Spring Web + Actuator |
| Containerização     | Docker            |
| Orquestração local  | Docker Compose    |
| Testes              | JUnit + Cucumber  |
| Versionamento       | GitHub            |

## 8. Arquitetura do Projeto

Ver detalhes atualizados em [docs/arquitetura.md](docs/arquitetura.md). Em resumo:

```
src/main/java/com/chiclete/reminder/
  domain/   → Entidades JPA
  service/  → Casos de uso
  infra/    → Repositórios
  ui/       → Controllers REST
  config/   → Segurança e JWT
```

## 9. Estrutura de Pastas do Repositório

```
/docs
  arquitetura.md
  hub.md
  relatorio-projeto.md
  pitch.md
  /requisitos
    requisitos-funcionais.md
    requisitos-nao-funcionais.md
  /testes
    plano-de-teste.md
/src/main/java/com/chiclete/reminder
  domain/
  infra/
  service/
  ui/
  config/
/src/main/resources/db/migration
docker-compose.yml
README.md
```

## 10. Como Executar o Projeto

1. **Subir o PostgreSQL** (na raiz do repositório):

```bash
docker compose up -d
```

2. **Executar a API** (porta padrão `8080`):

```bash
./mvnw spring-boot:run
```

Variáveis opcionais: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET` (mínimo **64 caracteres** para HS512).

Health check: `GET http://localhost:8080/actuator/health`

**Interface web (login e lembretes):** abre no navegador `http://localhost:8080/` — usa os mesmos endpoints da API (JWT guardado na sessão do navegador).

## 11. API (resumo)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/auth/register` | Cadastro (retorna JWT) |
| POST | `/api/auth/login` | Login (JWT) |
| GET | `/api/reminders` | Lista lembretes do utilizador (próprios + partilhados) |
| POST | `/api/reminders` | Cria lembrete |
| GET | `/api/reminders/{id}` | Detalhe |
| PUT | `/api/reminders/{id}` | Atualiza (dono) |
| DELETE | `/api/reminders/{id}` | Remove (dono) |
| PATCH | `/api/reminders/{id}/complete` | Corpo: `{"completed":true}` |
| PATCH | `/api/reminders/{id}/chewing` | Corpo: `{"chewing":true}` |
| POST | `/api/reminders/{id}/chewing/ignore` | Simula ignorar notificação (Modo Chiclete) |
| POST | `/api/reminders/{id}/share` | Corpo: `{"email":"..."}` (dono) |
| GET | `/api/groups` | Lista grupos em que o utilizador é membro |
| POST | `/api/groups` | Corpo: `{"name":"..."}` |
| POST | `/api/groups/{id}/members` | Corpo: `{"email":"..."}` |

Nas rotas protegidas, enviar cabeçalho `Authorization: Bearer <token>`.

## 12. Como Executar os Testes

```bash
./mvnw test
```

Utiliza H2 em memória e **não** necessita Docker. Cenários BDD (Cucumber) e integração MockMvc são executados na mesma suíte.

## 13. Metodologia

O projeto será desenvolvido utilizando a metodologia ágil **Scrum**, com organização do trabalho em Sprints, utilização de backlog, issues e quadro Kanban no GitHub.

### Alinhamento por sprint

- **Sprint 0 — setup:** visão e requisitos em `/docs`, arquitetura, Docker Compose, Flyway, stack alinhada no `pom.xml`, primeira feature técnica (domínio + evolução para API).
- **Sprint 1 — MVP + HUB:** autenticação JWT, CRUD de lembretes, testes de integração, BDD Cucumber; texto de apoio ao HUB em [docs/hub.md](docs/hub.md).
- **Sprint 2 — incremento + entregáveis:** partilha de lembretes, grupos, refinamento Modo Chiclete, [relatório](docs/relatorio-projeto.md) e [pitch](docs/pitch.md).

## 14. Integrantes do Projeto


| Nome              | Função na Sprint             | RA            |
| ----------------- | ---------------------------- | ------------- |
| Matheus Ferreira  | SM / Desenvolvedor Backend   | 4231924502    |
| Victor Hugo       | PO / Desenvolvedor Backend   | 42421886      |
| Vinicius Paiva    | Infra                        | 4231923132    |

## 15. Status do Projeto

**Backend REST funcional — escopo das Sprints 0–2 coberto no repositório** (cliente Android e notificações reais permanecem como evolução).
