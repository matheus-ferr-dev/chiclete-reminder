# 🧪✅ Plano de Teste

### 🆔📖 Identificação e contexto

| Campo                                       | Preencher ✍️ |
| ------------------------------------------- | ------------ |
| 🧩 Nome do projeto                          | **Chiclete** |
| 📝 Objetivo do sistema (resumo)             | Aplicativo de lembretes pessoais e compartilhados com **Modo Chiclete** (alertas persistentes até a conclusão), grupos familiares/equipe, preferências de canal (in-app, e-mail, WhatsApp simulado) e interface web em `http://localhost:8080/`. |
| 🎯 Público-alvo                             | Estudantes, famílias, casais, profissionais com rotina corrida e pequenas equipes de trabalho. |
| 💻 Plataforma/Tipo (console/web/mobile/API) | **Web** (SPA estática + API REST), **API** Spring Boot, **Mobile** Android (cliente em evolução). |
| 🔗 Repositório                              | https://github.com/matheus-ferr-dev/chiclete-reminder.git |
| 👥 Time/Grupo                               | Equipe Chiclete — metodologia Scrum, backlog e issues no GitHub. |

***

### 🎯🧪 Objetivo do teste

| Item                                 | Descrição 🗒️ |
| ------------------------------------ | ------------- |
| ✅ Objetivo geral                     | Garantir qualidade funcional e de regras de negócio do MVP (autenticação, lembretes, Modo Chiclete, grupos, compartilhamento, notificações e perfil), com cobertura automatizada (unitário, integração API, BDD) e validação qualitativa de usabilidade da central de alertas. |
| 📊 Metas de cobertura (se aplicável) | 100% dos requisitos funcionais RF01–RF09 com pelo menos um teste automatizado (UT, integração ou BDD); 7 cenários BDD executados em `./mvnw test`; relatório de usabilidade moderado com 3 participantes documentado. |

***

### 📦📌 Escopo

| Categoria                               | ✅ Em escopo | 🚫 Fora de escopo |
| --------------------------------------- | ----------- | ----------------- |
| 🧩 Funcionalidades                      | Cadastro, login, CRUD de lembretes, conclusão, Modo Chiclete, repetição, compartilhamento, grupos e convites, alertas (banner, sino, histórico), perfil e preferências de canal, estatísticas Chiclete. | Login social (Google/Apple/Android — apenas stub na UI). |
| 🧠 Regras de negócio                    | Alertas repetidos com Modo Chiclete; escala de prioridade após ignorados (`ignoreThreshold`); conclusão zera contador; visibilidade de lembretes compartilhados; convites de grupo. | Push nativo do navegador, SMS e envio real de WhatsApp. |
| 🔌 Integrações                          | API REST JSON + JWT; e-mail backend; simulação WhatsApp; polling de alertas (30 s) na SPA. | OAuth, WebSocket/SSE, integrações pagas de mensageria. |
| 🗃️ Dados                               | PostgreSQL (dev/prod local via Docker); H2 em memória nos testes automatizados; migrations Flyway. | Ambientes de produção gerenciados, backup/restore. |
| 🧑‍💻 Não-funcionais (usabilidade etc.) | Teste moderado de usabilidade (3 participantes); health check Actuator; responsividade mobile da SPA. | Testes de carga, pentest, acessibilidade WCAG formal. |

***

### 🧰🖥️ Ambiente e ferramentas

| Item                            | Especificação ⚙️ |
| ------------------------------- | ---------------- |
| 🖥️ SO                          | macOS / Linux / Windows (dev); navegadores Chrome, Safari, Firefox para testes manuais de UI. |
| ☕ Linguagem/Runtime             | Java 17+ (Spring Boot), JavaScript (SPA `app.js`), TypeScript/React (cliente web alternativo `chiclete-reminder-web/`). |
| 🧑‍💻 IDE                       | IntelliJ IDEA / VS Code / Cursor (conforme preferência do time). |
| 🧱 Build                        | Maven (`./mvnw`), Docker Compose (PostgreSQL), Vite (frontend React opcional). |
| ✅ Framework de testes unitários | JUnit 5, Spring Boot Test, MockMvc (integração API). |
| 🥒 BDD (se houver)              | Cucumber + Gherkin — [`cenarios-bdd.feature`](../../cenarios-bdd.feature); steps em `ReminderApiSteps.java`; runner `CucumberTestRunner.java`. |
| 🤖 CI (se houver)               | Não configurado no repositório; execução local `./mvnw test` (evidência em `target/surefire-reports/` e `target/cucumber-report.html`). |
| 🗄️ Banco/Dados (se houver)     | PostgreSQL 16 (Docker Compose); H2 in-memory nos testes (`application-test` / perfil de teste). |

***

### 🧪🧱 Estratégia de testes (por tipo)

| Tipo de teste         | 🎯 Objetivo | 📌 Escopo | 🛠️ Ferramenta | 👤 Responsável | 📎 Saída/Evidência |
| --------------------- | ----------- | --------- | -------------- | -------------- | ------------------ |
| ✅ Unitário            | Validar entidades, regras de recorrência e agendamento de alertas Chiclete. | `Reminder`, `RecurrenceSupport`, `ChicleteScheduler`. | JUnit 5 | Time de dev | `ReminderApplicationTests`, `RecurrenceSupportTest`, `ChicleteSchedulerTest` |
| 🌐 Sistema/End-to-End | Exercitar fluxos HTTP completos (auth → lembretes → grupos → perfil) contra contexto Spring. | Cadastro, Chiclete, share, grupos/convites, perfil, designação a membro, convite por token. | MockMvc + `@SpringBootTest` | Time de dev | `ReminderApiIntegrationTest` |
| 🥒 BDD                | Documentar e executar critérios de aceitação em linguagem de negócio. | 7 cenários: cadastro, login (ok/erro), Modo Chiclete, compartilhamento, grupos, conclusão. | Cucumber, `TestRestTemplate`, `RANDOM_PORT` | Time de dev / QA | [`cenarios-bdd.feature`](../../cenarios-bdd.feature), `target/cucumber-report.html` |
| 🧑‍💻 Usabilidade     | Validar descoberta e uso da central de alertas (banner, sino, histórico, preferências) no MVP web. | SPA em `:8080` — tarefas T1–T7 com think-aloud. | Sessões moderadas remotas | UX / pesquisa | [`relatorio-teste-usabilidade.md`](relatorio-teste-usabilidade.md) |

***

### 🧷🧭 Rastreabilidade (Requisitos x Testes)

| ID Req | Requisito/Funcionalidade | ⭐ Prioridade     | 🔗 Fonte (Issue/PR) | 🧪 IDs de testes (UT/BDD/RT) | 📌 Status                   |
| ------ | ------------------------ | ---------------- | ------------------- | ---------------------------- | --------------------------- |
| RF01   | Cadastro de usuário | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-01 / IT-cadastro | 🟢 Executado |
| RF02   | Login com JWT | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-02, BDD-03 | 🟢 Executado |
| RF03   | CRUD de lembretes | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | UT-01 / BDD-01 / IT-cadastro_listagem | 🟢 Executado |
| RF04   | Marcar lembrete como concluído | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | UT-03 / BDD-07 | 🟢 Executado |
| RF05   | Ativar/desativar Modo Chiclete | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | UT-02 / IT-chiclete_escala | 🟢 Executado |
| RF06   | Ignorar alerta e escalar prioridade | Alta | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-04 / IT-chiclete_escala / UT-06 | 🟢 Executado |
| RF07   | Compartilhar lembrete por e-mail | Média | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-05 / IT-compartilhar | 🟢 Executado |
| RF08   | Criar grupo e listar grupos | Média | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-06 / IT-grupos_convite | 🟢 Executado |
| RF09   | Adicionar membro ao grupo | Média | [requisitos-funcionais.md](../requisitos/requisitos-funcionais.md) | BDD-06 / IT-grupos_convite | 🟢 Executado |
| RNF05  | Testes automatizados | Alta | [requisitos-nao-funcionais.md](../requisitos/requisitos-nao-funcionais.md) | UT-01–UT-08 / BDD-01–BDD-07 | 🟢 Executado |
| UX-01  | Central de alertas e preferências (usabilidade) | Alta | [relatorio-teste-usabilidade.md](relatorio-teste-usabilidade.md) | RT-01–RT-07 | 🟢 Executado |

> 🏷️ Convenção sugerida:
>
> * ✅ **UT-XX** (teste unitário)
> * 🥒 **BDD-XX** (cenário BDD)
> * 📝 **RT-XX** (roteiro manual)

***

### 🧾🧪 Casos de teste planejados (resumo)

| ID     | 🧪 Tipo    | 🏷️ Título | 🔐 Pré-condição | 📥 Entrada | ✅ Resultado esperado | ⭐ Prioridade | 🤖 Automatizado? |
| ------ | ---------- | ---------- | --------------- | ---------- | -------------------- | ------------ | ---------------- |
| UT-01  | ✅ Unitário | Criar lembrete com campos corretos | — | Setters em `Reminder` | Getters retornam valores definidos | Alta | Sim |
| UT-02  | ✅ Unitário | Modo Chiclete desativado por padrão | — | Nova instância `Reminder` | `chewing == false` | Alta | Sim |
| UT-03  | ✅ Unitário | Marcar lembrete como concluído | Lembrete criado | `setCompleted(true)` | `isCompleted() == true` | Alta | Sim |
| UT-04  | ✅ Unitário | Recorrência dias úteis avança agenda | Data sexta-feira | `RecurrenceSupport.nextOccurrence…` | Próxima ocorrência na segunda | Média | Sim |
| UT-05  | ✅ Unitário | Recorrência dias personalizados | Dias 2, 4, 6 selecionados | Cálculo de próxima ocorrência | Respeita conjunto de dias | Média | Sim |
| UT-06  | ✅ Unitário | Scheduler repete alerta Chiclete após intervalo | Lembrete chewing vencido | Tick do scheduler | Nova notificação gerada | Alta | Sim |
| UT-07  | ✅ Unitário | Snooze bloqueia alerta temporariamente | Lembrete com `snoozedUntil` futuro | Tick do scheduler | Nenhum alerta até fim do snooze | Média | Sim |
| UT-08  | ✅ Unitário | Sem Chiclete não repete alerta | `chewing = false` | Tick do scheduler | Sem repetição | Média | Sim |
| BDD-01 | 🥒 BDD | Fluxo mínimo — cadastro + criar lembrete | API disponível | POST register + POST reminder | 201 e título correto | Alta | Sim |
| BDD-02 | 🥒 BDD | Login com credenciais válidas | Usuário cadastrado | POST login | 200 + JWT | Alta | Sim |
| BDD-03 | 🥒 BDD | Login com senha incorreta | Usuário cadastrado | POST login senha errada | 401 | Alta | Sim |
| BDD-04 | 🥒 BDD | Prioridade escala após 3 ignorados | Lembrete Chiclete BAIXA | POST chewing/ignore ×3 | `ignoreCount` 3, prioridade MEDIA | Alta | Sim |
| BDD-05 | 🥒 BDD | Convidado vê lembrete compartilhado | Owner compartilhou | GET reminders como guest | Lista contém título | Média | Sim |
| BDD-06 | 🥒 BDD | Membro convidado visualiza grupo | Grupo criado + membro adicionado | GET groups como convidado | Grupo visível | Média | Sim |
| BDD-07 | 🥒 BDD | Marcar lembrete como concluído | Lembrete existente | PATCH complete | `completed == true` | Alta | Sim |
| RT-01  | 📝 Manual | Criar lembrete Modo Chiclete na UI | Login na SPA `:8080` | Formulário novo lembrete | Lembrete na lista com badge Chiclete | Alta | Não |
| RT-02  | 📝 Manual | Adiar alerta pelo banner | Alerta Chiclete visível | Botão Adiar | Toast “Adiado 10 min”; banner atualiza | Alta | Não |
| RT-03  | 📝 Manual | Abrir sino e localizar alerta de lembrete | Alerta ativo | Clique no sino | Documentar expectativa vs. gaveta (convites vs. banner) | Alta | Não |
| RT-04  | 📝 Manual | Configurar preferências de canal no Perfil | Aba Perfil | Desmarcar e-mail, salvar | Preferências persistidas após reload | Média | Não |
| RT-05  | 📝 Manual | Consultar histórico de alertas | Alertas gerados anteriormente | Perfil → Histórico | Itens listados (lido/não lido) | Média | Não |
| RT-06  | 📝 Manual | Aceitar convite de grupo pelo sino | Convite pendente | Sino → Aceitar | Grupo visível; convite removido | Média | Não |
| RT-07  | 📝 Manual | Filtrar lembretes Chiclete e concluir | Lembretes chewing ativos | Filtro Chiclete + Concluir | Item move para concluídos | Média | Não |

***

### 🗃️🧪 Dados de teste

| ID    | 🧺 Conjunto | 📝 Descrição | 🧪 Como criar | 📍 Onde armazenar | 💡 Observações |
| ----- | ----------- | ------------ | ------------- | ----------------- | -------------- |
| DT-01 | H2 testes automatizados | Usuários e lembretes efêmeros por método de teste | `@Transactional` + register/login nos testes | Memória (H2) | Não requer Docker; usado em `./mvnw test`. |
| DT-02 | Cucumber / BDD | E-mails fixos por cenário (`bdd-user@test.com`, `login-ok@test.com`, etc.) | Steps em `ReminderApiSteps.java` | H2 / contexto Spring de teste | Um arquivo [`cenarios-bdd.feature`](../../cenarios-bdd.feature) na raiz. |
| DT-03 | PostgreSQL local | Dados persistentes para dev e testes manuais de UI | `docker compose up -d` + cadastro via SPA ou API | Volume Docker PostgreSQL | JWT em `localStorage` (`chiclete_token`). |
| DT-04 | Usabilidade | Contas com lembrete Chiclete vencido e convite de grupo pendente | Script manual / seed antes das sessões | Ambiente `:8080` branch `main` | Evidências em [`relatorio-teste-usabilidade.md`](relatorio-teste-usabilidade.md). |

***
