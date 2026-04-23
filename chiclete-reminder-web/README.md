# Chiclete Reminder — Web

Cliente web em React + TypeScript + Vite para consumir a API Spring Boot do monorepo.

## Requisitos

- Node.js 20+
- API em execução (por padrão `http://localhost:8080`)

## Configuração

Copie `.env.example` para `.env` e ajuste `VITE_API_URL` se a API não estiver na porta 8080.

## Scripts

```bash
npm install
npm run dev    # desenvolvimento
npm run build  # produção
npm run lint
```

## Rotas

- `/login` — autenticação (JWT guardado em `localStorage`)
- `/cadastro` — registro de usuário
- `/lembretes` — lista e criação de lembretes (rota protegida)
