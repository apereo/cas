#!/bin/bash

OFFA_CONTAINER="offa-oidc-federation-rp"

echo "Logs of OFFA container ${OFFA_CONTAINER}:"
docker logs --tail 500 "${OFFA_CONTAINER}" 2>&1 || echo "No logs are available for ${OFFA_CONTAINER}."
docker rm -f "${OFFA_CONTAINER}" >/dev/null 2>&1
exit 0
