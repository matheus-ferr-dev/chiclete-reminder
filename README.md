# Chiclete Reminder

## 1. Nome do Projeto

**Chiclete Reminder**

## 2. Resumo do Projeto

O Chiclete Reminder é um aplicativo de lembretes pessoais e compartilhados, desenvolvido com o objetivo de ajudar usuários a não esquecerem tarefas, compromissos e atividades importantes do dia a dia. O principal diferencial do sistema é o **Modo Chiclete**, funcionalidade que mantém o lembrete ativo e envia notificações recorrentes até que o usuário confirme a conclusão da tarefa. O sistema também permite a criação de grupos e o compartilhamento de lembretes entre usuários, tornando o aplicativo útil para famílias, estudantes e equipes de trabalho.

## 3. Problema que o Projeto Resolve

Muitas pessoas ignoram notificações simples de aplicativos de lembretes e acabam esquecendo tarefas importantes. Além disso, aplicativos tradicionais não garantem que o usuário realmente visualize ou conclua a tarefa. O Chiclete Reminder busca resolver esse problema por meio de lembretes persistentes e compartilháveis, que acompanham o usuário até a conclusão da atividade.

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
| Containerização     | Docker            |
| Orquestração local  | Docker Compose    |
| Testes              | JUnit + Cucumber  |
| Versionamento       | GitHub            |

## 8. Arquitetura do Projeto

O projeto será organizado em camadas, seguindo boas práticas de engenharia de software:

```
src/
  domain/   -> Entidades e regras de negócio
  service/  -> Casos de uso e regras do sistema
  infra/    -> Persistência de dados
  ui/       -> Interface do usuário
```

## 9. Estrutura de Pastas do Repositório

```
/docs
  /requisitos
    requisitos-funcionais.md
    requisitos-nao-funcionais.md
  /testes
    plano-de-teste.md
    roteiros-de-teste.md
    usabilidade.md
    /evidencias
/slides
/src
README.md
```

## 10. Como Executar o Projeto

*(Instruções serão adicionadas nas próximas sprints.)*

## 11. Como Executar os Testes

*(Instruções serão adicionadas nas próximas sprints.)*

## 12. Metodologia

O projeto será desenvolvido utilizando a metodologia ágil **Scrum**, com organização do trabalho em Sprints, utilização de backlog, issues e quadro Kanban no GitHub.

## 13. Integrantes do Projeto


| Nome              | Função na Sprint             | RA            |
| ----------------- | ---------------------------- | ------------- |
| Matheus Ferreira  | SM / Desenvolvedor Backend   | 4231924502    |
| Victor Hugo       | PO / Desenvolvedor Backend   | 42421886      |
| Vinicius Paiva    | Infra                        | 4231923132    |

## 14. Status do Projeto

**Projeto em desenvolvimento – Sprint 0.**
