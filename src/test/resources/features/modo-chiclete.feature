# language: pt
Funcionalidade: Modo Chiclete
  Como usuário
  Quero lembretes persistentes com o Modo Chiclete
  Para não esquecer tarefas importantes

  Cenário: Prioridade escala após ignorar notificações
    Dado que estou autenticado como "chiclete@test.com"
    Quando crio um lembrete "Tomar remédio" com modo chiclete ativo e prioridade "BAIXA"
    E ignoro o lembrete chiclete "Tomar remédio" 3 vezes
    Então o lembrete "Tomar remédio" tem ignoreCount 3
    E a prioridade do lembrete "Tomar remédio" é "MEDIA"
