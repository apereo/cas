#!/bin/sh
set -eu

API_URL="${WALLET_API_URL:-http://wallet-api:7001}"
EMAIL="${WALLET_EMAIL:-alice@example.org}"
PASSWORD="${WALLET_PASSWORD:-password}"
NAME="${WALLET_NAME:-Alice}"

echo "Registering test account..."

response="$(
  curl --silent --show-error \
    --write-out '\n%{http_code}' \
    --request POST \
    --header 'Content-Type: application/json' \
    --data "{
      \"name\": \"${NAME}\",
      \"email\": \"${EMAIL}\",
      \"password\": \"${PASSWORD}\",
      \"type\": \"email\"
    }" \
    "${API_URL}/wallet-api/auth/register"
)"

body="$(printf '%s\n' "$response" | sed '$d')"
status="$(printf '%s\n' "$response" | tail -n 1)"

case "$status" in
  200|201)
    echo "Wallet account created: ${EMAIL}"
    ;;
  400|409)
    echo "Wallet account probably already exists: ${EMAIL}"
    ;;
  *)
    echo "Wallet registration failed with HTTP ${status}"
    echo "$body"
    exit 1
    ;;
esac
