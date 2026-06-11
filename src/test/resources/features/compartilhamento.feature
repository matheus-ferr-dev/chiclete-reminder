# language: pt
Funcionalidade: Compartilhamento de lembretes
  Como usuário
  Quero compartilhar lembretes com outras pessoas
  Para que todos acompanhem as tarefas

  Cenário: Convidado vê lembrete compartilhado
    Dado que "owner@test.com" criou o lembrete "Reunião"
    E "owner@test.com" compartilhou o lembrete "Reunião" com "guest@test.com"
    Quando "guest@test.com" lista seus lembretes
    Então vê 1 lembrete com título "Reunião"
