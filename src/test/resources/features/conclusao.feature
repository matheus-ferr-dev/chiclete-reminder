# language: pt
Funcionalidade: Conclusão de lembretes
  Como usuário
  Quero marcar lembretes como concluídos
  Para indicar que a tarefa foi feita

  Cenário: Marcar lembrete como concluído
    Dado que estou autenticado como "conclusao@test.com"
    Quando crio um lembrete "Entregar trabalho" com prioridade "ALTA"
    E marco o lembrete "Entregar trabalho" como concluído
    Então o lembrete "Entregar trabalho" está concluído
