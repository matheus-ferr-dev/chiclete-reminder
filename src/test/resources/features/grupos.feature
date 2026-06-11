# language: pt
Funcionalidade: Grupos
  Como usuário
  Quero criar grupos e adicionar membros
  Para organizar lembretes em família ou equipe

  Cenário: Membro convidado visualiza o grupo
    Dado que "alfa@test.com" criou o grupo "Família"
    E "alfa@test.com" adicionou "beta@test.com" ao grupo "Família"
    Quando "beta@test.com" lista seus grupos
    Então vê o grupo "Família"
