# Relatório do projeto — Chiclete

## 1. Introdução

O Chiclete é um sistema de lembretes com **Modo Chiclete** (persistência de urgência até a conclusão), cadastro de usuários, compartilhamento por e-mail e grupos. Este relatório descreve a implementação atual do **backend REST** em Spring Boot.

## 2. Objetivos alcançados

- API com autenticação JWT e CRUD de lembretes.
- Regra de negócio do Modo Chiclete modelada (contagem de ignorados e escalação de prioridade).
- Compartilhamento e grupos persistidos em PostgreSQL com migrations Flyway.
- Testes automatizados (integração e BDD com Cucumber).

## 3. Arquitetura

Camadas `domain`, `infra` (repositórios JPA), `service`, `ui` (controllers), `config` (segurança e JWT). Detalhes em [arquitetura.md](arquitetura.md).

## 4. Metodologia e sprints

- **Sprint 0:** documentação, stack, Docker Compose, Flyway, primeira camada de dados e testes iniciais.
- **Sprint 1 (MVP):** cadastro/login, lembretes, testes de integração e cenário BDD; evidências de HUB documentadas em [hub.md](hub.md).
- **Sprint 2:** compartilhamento, grupos, refinamento da regra Chiclete, relatório e pitch.

## 5. Limitações e próximos passos

- Notificações push e agendador não estão no backend; o endpoint `chewing/ignore` simula o “ignorado” para testes e demos.
- Cliente Android (previsto no README) é trabalho de front separado consumindo esta API.

## 6. Conclusão

O backend está alinhado ao escopo funcional principal do README e coberto por testes, servindo como base para o app mobile e integrações futuras.
