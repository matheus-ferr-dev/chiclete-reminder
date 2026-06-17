#!/usr/bin/env bash
# Expõe a API/web local (porta 8080) via Cloudflare Quick Tunnel (link público temporário).
set -euo pipefail

PORT="${1:-8080}"
URL="http://localhost:${PORT}"

if ! command -v cloudflared >/dev/null 2>&1; then
  echo "cloudflared não encontrado. Instale com: brew install cloudflared"
  exit 1
fi

if ! curl -sf "${URL}/actuator/health" >/dev/null 2>&1; then
  echo "Nada respondendo em ${URL}"
  echo "Suba o ambiente antes:"
  echo "  docker compose up -d"
  echo "  ./mvnw spring-boot:run"
  exit 1
fi

echo "A criar túnel Cloudflare para ${URL}..."
echo "Partilhe o link *.trycloudflare.com que aparecer abaixo."
echo "Ctrl+C para encerrar o túnel."
echo

exec cloudflared tunnel --url "${URL}"
