# language: pt
# Chiclete Reminder — cenários BDD (Cucumber/Gherkin)
# Step definitions: src/test/java/com/chiclete/reminder/bdd/ReminderApiSteps.java

Funcionalidade: Cadastro e lembretes
  Como usuário
  Quero me cadastrar e criar lembretes
  Para acompanhar minhas tarefas com o Chiclete

  Cenário: Fluxo mínimo de API
    Dado a API está disponível
    Quando me cadastro com email "bdd-user@test.com" e senha "senha123"
    Então o cadastro retorna 201
    Quando crio um lembrete "Estudar para prova" com prioridade "ALTA"
    Então a criação do lembrete retorna 201
    E o lembrete criado tem título "Estudar para prova"

Funcionalidade: Autenticação
  Como usuário
  Quero fazer login no sistema
  Para acessar meus lembretes

  Cenário: Login com credenciais válidas
    Dado que existe um usuário cadastrado com email "login-ok@test.com" e senha "senha123"
    Quando faço login com email "login-ok@test.com" e senha "senha123"
    Então o login retorna 200
    E a resposta contém um token JWT

  Cenário: Login com senha incorreta
    Dado que existe um usuário cadastrado com email "login-fail@test.com" e senha "senha123"
    Quando faço login com email "login-fail@test.com" e senha "errada"
    Então o login retorna 401

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

Funcionalidade: Compartilhamento de lembretes
  Como usuário
  Quero compartilhar lembretes com outras pessoas
  Para que todos acompanhem as tarefas

  Cenário: Convidado vê lembrete compartilhado
    Dado que "owner@test.com" criou o lembrete "Reunião"
    E "owner@test.com" compartilhou o lembrete "Reunião" com "guest@test.com"
    Quando "guest@test.com" lista seus lembretes
    Então vê 1 lembrete com título "Reunião"

Funcionalidade: Grupos
  Como usuário
  Quero criar grupos e adicionar membros
  Para organizar lembretes em família ou equipe

  Cenário: Membro convidado visualiza o grupo
    Dado que "alfa@test.com" criou o grupo "Família"
    E "alfa@test.com" adicionou "beta@test.com" ao grupo "Família"
    Quando "beta@test.com" lista seus grupos
    Então vê o grupo "Família"

Funcionalidade: Conclusão de lembretes
  Como usuário
  Quero marcar lembretes como concluídos
  Para indicar que a tarefa foi feita

  Cenário: Marcar lembrete como concluído
    Dado que estou autenticado como "conclusao@test.com"
    Quando crio um lembrete "Entregar trabalho" com prioridade "ALTA"
    E marco o lembrete "Entregar trabalho" como concluído
    Então o lembrete "Entregar trabalho" está concluído
