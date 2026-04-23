# Chiclete Reminder

## 1. Nome do Projeto
./mvnw spring-boot:run
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
| Mobile              | Android (Java) + Retrofit  |
| Web                 | Vite + React + TypeScript  |
| Backend             | Spring Boot                |
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

Estrutura no workspace:

- `chiclete-reminder/` — API Spring Boot (porta padrão **8080**)
- `chiclete-reminder-web/` — interface web
- `chiclete-reminder-android/` — app Android (abrir no **Android Studio**)

### 10.1. Backend (API)

1. Subir o PostgreSQL (banco e credenciais em `src/main/resources/application.properties`).
2. No diretório `chiclete-reminder/`:  
   `./mvnw spring-boot:run`  
3. A API fica em `http://localhost:8080/`.

O backend está com **CORS** liberado para `http://localhost:*` e `http://127.0.0.1:*` (necessário para o front web no Vite, outra origem).

### 10.2. Web (Vite + React)

1. Com a API em execução:  
   `cd chiclete-reminder-web`  
2. (Opcional) copiar `cp .env.example .env` e ajustar `VITE_API_URL` se a API não estiver em `http://localhost:8080`.  
3. `npm install` (primeira vez) e `npm run dev`.  
4. Abrir o endereço mostrado no terminal (em geral `http://localhost:5173`).

### 10.3. Android

1. Abrir a pasta `chiclete-reminder-android` no **Android Studio** (a IDE pode criar/ajustar o Gradle Wrapper e sincronizar o projeto).  
2. A URL padrão da API no emulador aponta para o host com **`http://10.0.2.2:8080/`** (é o `localhost` do computador visto de dentro do emulador).  
3. Garanta o backend rodando no Mac/PC; no emulador, use **Entrar** / **Cadastrar** e depois **Carregar lembretes**.  
4. Dispositivo físico na mesma Wi‑Fi: no `app/build.gradle`, altere `buildConfigField "API_BASE"` para `http://<IP-da-sua-máquina>:8080/` e reconstrua o app.

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
