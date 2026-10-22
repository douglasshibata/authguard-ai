#!/bin/bash
# AuthGuard AI - CLI Demo Script
# Fluxo: setup -> verify (TOTP + risk engine).
# Requer: backend em :8080 e ai-engine em :8000.

set -euo pipefail

BACKEND="${BACKEND_URL:-http://localhost:8080}"
ACCOUNT="${ACCOUNT:-demo@authguard.ai}"

echo "AuthGuard AI demo"
echo "------------------------------------------"

echo "[1/3] Setup TOTP para $ACCOUNT"
SETUP=$(curl -s --fail "$BACKEND/api/totp/setup?account=$ACCOUNT")
echo "  $SETUP"
SECRET=$(echo "$SETUP" | grep -o '"secret":"[^"]*"' | cut -d'"' -f4)
echo "  secret = $SECRET"

echo
echo "[2/3] Verify com token placeholder (substitua pelo código atual do seu app autenticador)"
TOKEN="${TOKEN:-123456}"
VERIFY=$(curl -s -X POST "$BACKEND/api/totp/verify" \
    -H 'Content-Type: application/json' \
    -d "{\"account\":\"$ACCOUNT\",\"secret\":\"$SECRET\",\"token\":\"$TOKEN\"}")
echo "  $VERIFY"

echo
echo "[3/3] Health check"
curl -s "$BACKEND/actuator/health"
echo

echo "------------------------------------------"
echo "Fim. Para fluxo real: gere TOTP no Google Authenticator/Authy com o secret acima e rode novamente com TOKEN=<codigo>."
