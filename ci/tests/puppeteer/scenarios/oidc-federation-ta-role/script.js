const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

const ENTITY_CONFIGURATION_URL = "https://localhost:8443/cas/oidc/.well-known/openid-federation";
const ACTUATOR_URL = "https://localhost:8443/cas/actuator/oidcFederationJwks";

async function fetchEntityConfiguration() {
    const statement = await cas.doRequest(ENTITY_CONFIGURATION_URL, "GET", {}, 200);
    const keys = jose.createLocalJWKSet(jose.decodeJwt(statement).jwks);
    const {payload, protectedHeader} = await jose.jwtVerify(statement, keys, {typ: "entity-statement+jwt"});
    return {payload, kid: protectedHeader.kid, keyIds: payload.jwks.keys.map((key) => key.kid)};
}

/**
 * Key rollover (OpenID Federation 1.0, section 11.2) through the actuator endpoint: the trust anchor signs with a key it
 * published before the rotation and still publishes the key it signed with before; once retired, that key is published
 * by the historical keys endpoint (section 8.7) only.
 */
async function verifyKeyRollover() {
    await cas.log("Trust anchor: key rollover and historical keys");
    let before = await fetchEntityConfiguration();
    if (before.keyIds.length < 2) {
        await cas.log("The keystore predates rotation: the first rotation only publishes a future key");
        await cas.doRequest(`${ACTUATOR_URL}/rotate`, "POST", {"Content-Type": "application/json"}, 200, "");
        const published = await fetchEntityConfiguration();
        assert.equal(published.kid, before.kid);
        before = published;
    }
    assert(before.keyIds.length >= 2, "A future key must be published before the rotation");
    await cas.doRequest(`${ACTUATOR_URL}/rotate`, "POST", {"Content-Type": "application/json"}, 200, "");
    const rotated = await fetchEntityConfiguration();
    assert.notEqual(rotated.kid, before.kid);
    assert(before.keyIds.includes(rotated.kid), "The new signing key must have been published before the rotation");
    assert(rotated.keyIds.includes(before.kid), "The previous signing key must stay published after the rotation");

    await cas.doRequest(`${ACTUATOR_URL}/revoke`, "POST", {"Content-Type": "application/json"}, 200, "");
    const revoked = await fetchEntityConfiguration();
    assert(!revoked.keyIds.includes(before.kid));

    const historicalKeysUrl = revoked.payload.metadata.federation_entity.federation_historical_keys_endpoint;
    assert.equal(historicalKeysUrl, "https://localhost:8443/cas/oidc/federation_historical_keys");
    const historical = await cas.doRequest(historicalKeysUrl, "GET", {}, 200);
    const {payload, protectedHeader} = await jose.jwtVerify(historical, jose.createLocalJWKSet(revoked.payload.jwks),
        {typ: "jwk-set+jwt", issuer: "https://localhost:8443/cas/oidc"});
    assert.equal(protectedHeader.kid, revoked.kid);
    const retired = payload.keys.find((key) => key.kid === before.kid);
    assert(retired !== undefined, "The retired key must be published as a historical key");
    assert(retired.exp !== undefined);
    assert.equal(retired.d, undefined);
}

const SUBORDINATE_KEYS = ["TJns"];

(async () => {

    const endpointUrl = ENTITY_CONFIGURATION_URL;
    const jwt = await cas.doGet(endpointUrl,
        (res) => res.data,
        (error) => {
            throw `Federation endpoint not available: ${error}`;
        });

    const decoded = await cas.decodeJwt(jwt);
    await cas.log(decoded);
    assert(decoded.iss !== undefined);
    assert(decoded.sub !== undefined);
    assert(decoded.exp !== undefined);
    assert(decoded.jwks !== undefined);

    const metadata = decoded.metadata;
    const federationEntity = metadata["federation_entity"];
    assert(federationEntity !== undefined);
    assert(metadata["openid_provider"] === undefined);
    assert(decoded["authority_hints"] === undefined);

    assert(federationEntity["organization_name"] === "ApereoTA");
    assert(federationEntity["federation_fetch_endpoint"] === "https://localhost:8443/cas/oidc/fetch");
    assert(federationEntity["federation_list_endpoint"] === "https://localhost:8443/cas/oidc/list");
    assert(federationEntity["contacts"] !== undefined);
    assert(decoded.constraints === undefined);
    assert.equal(decoded.exp - decoded.iat, 90 * 24 * 60 * 60);

    const fetchUrl = "https://localhost:8443/cas/oidc/fetch?sub=http%3A%2F%2Frp";
    const jwt2 = await cas.doGet(fetchUrl,
        (res) => res.data,
        (error) => {
            throw `Fetch endpoint not available: ${error}`;
        });

    const decoded2 = await cas.decodeJwt(jwt2);
    await cas.log(decoded2);
    assert(decoded2.iss === "https://localhost:8443/cas/oidc");
    assert(decoded2.sub === "http://rp");
    assert(decoded2.exp !== undefined);
    assert(decoded2.jwks !== undefined);

    const metadata2 = decoded2.metadata;
    assert(metadata2["federation_entity"] === undefined);
    assert(metadata2["openid_provider"] === undefined);
    assert(metadata2["openid_relying_party"] !== undefined);
    assert(decoded2["authority_hints"] === undefined);
    assert.deepEqual(decoded2.jwks.keys.map((key) => key.kid), SUBORDINATE_KEYS);

    const unknown = await cas.doRequest("https://localhost:8443/cas/oidc/fetch?sub=https%3A%2F%2Funknown.example.org", "GET", {}, 404);
    assert.equal(JSON.parse(unknown).error, "not_found");
    const own = await cas.doRequest(`https://localhost:8443/cas/oidc/fetch?sub=${encodeURIComponent(decoded.iss)}`, "GET", {}, 400);
    assert.equal(JSON.parse(own).error, "invalid_request");

    const listUrl = "https://localhost:8443/cas/oidc/list";
    await cas.doGet(listUrl,
        (res) => res.data,
        (error) => {
            throw `List endpoint not available: ${error}`;
        });

    await verifyKeyRollover();

})();
