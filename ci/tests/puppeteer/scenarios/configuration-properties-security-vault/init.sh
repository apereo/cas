#!/bin/bash
set -e
clear
echo Starting Vault
docker stop vault || true && docker rm vault || true
docker run -d --rm -e VAULT_SKIP_VERIFY=true \
  -e VAULT_DEV_ROOT_TOKEN_ID=TorGUGeNTATePrE \
  -p 8200:8200 \
  --cap-add=IPC_LOCK --name=vault vault:latest
sleep 5

VARS="export VAULT_ADDR=http://127.0.0.1:8200; export VAULT_TOKEN=TorGUGeNTATePrE;"
docker exec vault sh -c "${VARS} vault kv put secret/cas/vault cas.authn.accept.users=securecas::paSSw0rd"
echo Vault started
