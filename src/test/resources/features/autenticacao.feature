# language: pt
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
