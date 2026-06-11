# Requisitos não funcionais — Chiclete

| ID | Requisito | Como é atendido |
|----|-----------|-----------------|
| RNF01 | API REST com JSON | Spring Web |
| RNF02 | Persistência relacional | PostgreSQL + JPA + Flyway |
| RNF03 | Autenticação stateless | JWT (Bearer) + Spring Security |
| RNF04 | Ambiente reprodutível local | Docker Compose (PostgreSQL) |
| RNF05 | Testes automatizados | JUnit 5, testes de integração MockMvc, Cucumber (BDD) |
| RNF06 | Observabilidade básica | Spring Actuator (`/actuator/health`) |

## Segurança

- Senhas armazenadas com BCrypt.
- Endpoints `/api/**` (exceto autenticação) exigem token válido.
- Respostas 404 para lembretes invisíveis ao usuário (não vazar existência).
