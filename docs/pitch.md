# Pitch — Chiclete Reminder (roteiro sugerido)

**Problema:** pessoas ignoram notificações únicas e esquecem o que importa.

**Solução:** lembretes com **Modo Chiclete** — o sistema mantém a urgência (repetição simulada e prioridade que sobe quando o usuário adia).

**Diferenciais demonstráveis na API**

- Login rápido com JWT.
- Dono compartilha lembrete por e-mail com outro usuário.
- Grupos para famílias ou equipes.
- Endpoint que mostra a prioridade subindo após vários “ignorados”.

**Demo (2 minutos)**

1. Subir PostgreSQL: `docker compose up -d`.
2. Subir API: `./mvnw spring-boot:run`.
3. Registrar dois usuários, criar lembrete com Modo Chiclete, chamar `chewing/ignore` algumas vezes e mostrar a prioridade alterada no JSON.

**Encerramento:** próximo passo é o app Android consumindo a mesma API.

_Ficheiro de apoio para slides orais; exporte para PDF/PPT conforme a disciplina exija._
