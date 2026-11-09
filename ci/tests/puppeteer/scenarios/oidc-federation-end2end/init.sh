#!/bin/bash
#
# Starts the go-oidfed OFFA relying party (https://localhost) for the OpenID Federation end-to-end scenario and
# prepares what the CAS trust anchor needs before it starts: a truststore with the shared localhost certificate,
# the TLS key pair for OFFA and the scenario's Node RP, and the subordinate statements (op.json and rp.json from this
# scenario plus offa.json, built from OFFA's live entity configuration since OFFA generates its keys on first start).
# OFFA and the Node RP carry a relying party trust mark of the trust anchor, which the OP requires. The Node RP fetches its
# own from the trust anchor's trust mark endpoint; OFFA starts before the trust anchor, so its trust mark is signed here
# with the trust anchor's key (trust-mark.js), and the trust anchor's status endpoint vouches for it since OFFA is one of
# the subjects it issues the type to. OFFA's is added as extra entity configuration data, since go-oidfed refuses a
# configured trust mark JWT it cannot verify.
# The OP's service registry is a fresh copy of this scenario's services, since relying parties registered automatically
# are written there and would otherwise carry the keys of an earlier run into the next one.
# run.sh sources this script once per CAS instance; the second call finds OFFA running and does nothing.
#

export TMPDIR="${TMPDIR:-/tmp}"
SCENARIO_DIR="${PWD}/ci/tests/puppeteer/scenarios/${SCENARIO}"
WORK_DIR="${PWD}/ci/tests/puppeteer/overlay/${SCENARIO}"
OFFA_DIR="${WORK_DIR}/offa"
SUBORDINATES_DIR="${WORK_DIR}/subordinates"
SERVICES_DIR="${WORK_DIR}/services"
TRUSTSTORE_FILE="${WORK_DIR}/federation-truststore.p12"
TRUSTSTORE_PASSWORD="changeit"
CAS_KEYSTORE_FILE="${CAS_KEYSTORE:-${PWD}/ci/tests/puppeteer/overlay/thekeystore}"
CAS_CERT_FILE="${CAS_CERT:-${PWD}/ci/tests/puppeteer/overlay/server.crt}"
OFFA_CONTAINER="offa-oidc-federation-rp"
export DOCKER_IMAGE="oidfed/offa:0.7.0"
export OFFA_IMAGE="${DOCKER_IMAGE}"
OFFA_ENTITY_ID="https://localhost"
TRUST_ANCHOR="https://localhost:8443/cas/oidc"

offa_is_running() {
  curl --output /dev/null --silent --fail --insecure "${OFFA_ENTITY_ID}/.well-known/openid-federation"
}

fail_with_diagnostics() {
  echo "$1"
  docker inspect -f 'State: {{.State.Status}}, exit code: {{.State.ExitCode}}, error: {{.State.Error}}' "${OFFA_CONTAINER}" 2>&1
  docker logs --tail 200 "${OFFA_CONTAINER}" 2>&1
  return 1
}

prepare_tls() {
  mkdir -p "${WORK_DIR}" "${OFFA_DIR}/keys" "${SUBORDINATES_DIR}"
  rm -f "${TRUSTSTORE_FILE}" "${WORK_DIR}/cas.p12"
  keytool -importcert -noprompt -trustcacerts -alias cas -file "${CAS_CERT_FILE}" \
    -keystore "${TRUSTSTORE_FILE}" -storepass "${TRUSTSTORE_PASSWORD}" -storetype PKCS12 || return 1
  export_key_pair || export_key_pair -J-Dkeystore.pkcs12.legacy || return 1
  cp "${WORK_DIR}/tls.key" "${WORK_DIR}/tls.crt" "${OFFA_DIR}/"
}

# OpenSSL 3 reads the PKCS12 files of current JDKs; LibreSSL (macOS) only reads the legacy format.
export_key_pair() {
  rm -f "${WORK_DIR}/cas.p12"
  keytool "$@" -importkeystore -noprompt -srckeystore "${CAS_KEYSTORE_FILE}" -srcstorepass changeit -srcalias cas \
    -destkeystore "${WORK_DIR}/cas.p12" -deststoretype PKCS12 -deststorepass changeit -destkeypass changeit || return 1
  openssl pkcs12 -in "${WORK_DIR}/cas.p12" -passin pass:changeit -nocerts -nodes -out "${WORK_DIR}/tls.key" 2>/dev/null &&
    openssl pkcs12 -in "${WORK_DIR}/cas.p12" -passin pass:changeit -clcerts -nokeys -out "${WORK_DIR}/tls.crt" 2>/dev/null
}

write_offa_config() {
  local trustAnchorKeys
  trustAnchorKeys=$(python3 -c '
import json, sys
keys = json.load(open(sys.argv[1]))["keys"][:1]
private = {"d", "p", "q", "dp", "dq", "qi"}
print(json.dumps({"keys": [{k: v for k, v in key.items() if k not in private} for key in keys]}))
' "${SCENARIO_DIR}/metadata/ta-federation.jwks") || return 1
  local offaTrustMark
  offaTrustMark=$(node "${SCENARIO_DIR}/trust-mark.js" "${SCENARIO_DIR}/metadata/ta-federation.jwks" "${OFFA_ENTITY_ID}") || return 1
  cat > "${OFFA_DIR}/config.yaml" <<YAML
server:
  trusted_proxies: ["127.0.0.1", "::1"]
  tls:
    enabled: true
    redirect_http: false
    cert: /data/tls.crt
    key: /data/tls.key
logging:
  access:
    stderr: true
  internal:
    level: debug
    stderr: true
sessions:
  ttl: 3600
signing:
  key_storage: /data/keys
  federation:
    alg: ES256
  oidc:
    algs: [RS256]
    default_alg: RS256
federation:
  entity_id: ${OFFA_ENTITY_ID}
  client_name: CAS OFFA scenario
  organization_name: Apereo CAS
  scopes: [openid, profile, email]
  client_registration_types: [automatic]
  configuration_lifetime: 1d
  trust_anchors:
    - entity_id: ${TRUST_ANCHOR}
      jwks: ${trustAnchorKeys}
  authority_hints:
    - ${TRUST_ANCHOR}
  extra_entity_configuration_data:
    trust_marks:
      - trust_mark_type: ${TRUST_ANCHOR}/trust-marks/relying-party
        trust_mark: ${offaTrustMark}
op_discovery:
  local:
    enabled: true
    entity_collection_interval: 10
YAML
}

start_offa() {
  docker rm -f "${OFFA_CONTAINER}" >/dev/null 2>&1
  rm -rf "${OFFA_DIR}/keys" && mkdir -p "${OFFA_DIR}/keys"
  echo "Starting OFFA container ${OFFA_CONTAINER} from ${OFFA_IMAGE} on port 443 (host networking)"
  docker run --quiet -d --name "${OFFA_CONTAINER}" --network=host \
    -v "${OFFA_DIR}":/data \
    -v "${WORK_DIR}/tls.crt":/usr/local/share/ca-certificates/cas-localhost.crt:ro \
    "${OFFA_IMAGE}" || return 1
  for ((counter = 1; counter <= 90; counter++)); do
    if offa_is_running; then
      echo "OFFA started at ${OFFA_ENTITY_ID}"
      return 0
    fi
    if ! docker inspect -f '{{.State.Running}}' "${OFFA_CONTAINER}" 2>/dev/null | grep -q true; then
      fail_with_diagnostics "OFFA container ${OFFA_CONTAINER} terminated before serving its entity configuration"
      return 1
    fi
    sleep 2
  done
  fail_with_diagnostics "Timed out waiting for OFFA at ${OFFA_ENTITY_ID}"
}

write_subordinates() {
  cp "${SCENARIO_DIR}"/subordinates/*.json "${SUBORDINATES_DIR}/"
  curl --silent --fail --insecure "${OFFA_ENTITY_ID}/.well-known/openid-federation" | python3 -c '
import base64, json, sys
payload = sys.stdin.read().strip().split(".")[1]
claims = json.loads(base64.urlsafe_b64decode(payload + "=" * (-len(payload) % 4)))
subordinate = {
    "entityId": claims["sub"],
    "description": "go-oidfed OFFA relying party",
    "metadata": {"openid_relying_party": {}},
    "federationKeys": claims["jwks"]["keys"],
}
json.dump(subordinate, open(sys.argv[1], "w"), indent=2)
' "${SUBORDINATES_DIR}/offa.json" || return 1
  echo "Subordinates for the trust anchor: $(ls "${SUBORDINATES_DIR}")"
}

write_services() {
  rm -rf "${SERVICES_DIR}" && mkdir -p "${SERVICES_DIR}"
  cp "${SCENARIO_DIR}"/services/*.json "${SERVICES_DIR}/"
}

if offa_is_running && [[ -f "${SUBORDINATES_DIR}/offa.json" ]]; then
  echo "OFFA is already running at ${OFFA_ENTITY_ID}"
  initExitCode=0
else
  prepare_tls && write_services && write_offa_config && start_offa && write_subordinates
  initExitCode=$?
fi
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  exit "${initExitCode}"
fi
return "${initExitCode}"
