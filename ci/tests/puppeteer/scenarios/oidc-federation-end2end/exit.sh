#!/bin/bash

CONTAINER_NAME="pac4j-oidc-federation-rp"

echo "Logs of pac4j demo container ${CONTAINER_NAME}:"
docker logs --tail 500 "${CONTAINER_NAME}" 2>&1 || echo "No logs are available for ${CONTAINER_NAME}."
