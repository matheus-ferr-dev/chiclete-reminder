# language: pt
Funcionalidade: Cadastro e lembretes
  Como usuário
  Quero me cadastrar e criar lembretes
  Para acompanhar minhas tarefas com o Chiclete Reminder

  Cenário: Fluxo mínimo de API
    Dado a API está disponível
    Quando me cadastro com email "bdd-user@test.com" e senha "senha123"
    Então o cadastro retorna 201
    Quando crio um lembrete "Estudar para prova" com prioridade "ALTA"
    Então a criação do lembrete retorna 201
    E o lembrete criado tem título "Estudar para prova"
