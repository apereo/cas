const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

const ISSUER = "https://localhost:8443/cas/oidc";
const JWKS_ENDPOINT = `${ISSUER}/jwks`;
const ACTUATOR = "https://localhost:8443/cas/actuator/oidcJwks";

async function fetchKeys(state = undefined) {
    const url = state === undefined ? JWKS_ENDPOINT : `${JWKS_ENDPOINT}?state=${state}`;
    const body = await cas.doRequest(url, "GET", {}, 200);
    return JSON.parse(body).keys;
}

async function fetchKeyStates() {
    const states = {
        current: await fetchKeys("current"),
        future: await fetchKeys("future"),
        previous: await fetchKeys("previous")
    };
    const kids = (keys, use) => keys.filter((key) => key.use === use).map((key) => key.kid);
    const result = {
        currentSigning: kids(states.current, "sig"),
        currentEncryption: kids(states.current, "enc"),
        futureSigning: kids(states.future, "sig"),
        futureEncryption: kids(states.future, "enc"),
        previous: states.previous.map((key) => key.kid)
    };
    await cas.log(`Key states: ${JSON.stringify(result)}`);
    return result;
}

async function issueIdToken() {
    const params = new URLSearchParams({grant_type: "client_credentials", scope: "openid"});
    return cas.doPost(`${ISSUER}/token`, params, {
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": `Basic ${Buffer.from("client:secret").toString("base64")}`
    }, (res) => {
        assert(res.data.id_token !== undefined, "No ID token is issued");
        return res.data.id_token;
    }, (error) => {
        throw `Unable to obtain an ID token: ${error}`;
    });
}

async function issueIdTokenSignedWith(kid) {
    for (let attempt = 1; attempt <= 10; attempt++) {
        const idToken = await issueIdToken();
        const header = jose.decodeProtectedHeader(idToken);
        if (header.kid === kid) {
            return idToken;
        }
        await cas.log(`Attempt ${attempt}: ID token is signed with ${header.kid}; waiting for ${kid}`);
        await cas.sleep(1000);
    }
    throw `No ID token is signed with the current signing key ${kid}`;
}

async function verifyWithPublishedKeys(idToken) {
    const jwks = jose.createLocalJWKSet({keys: await fetchKeys()});
    const result = await jose.jwtVerify(idToken, jwks, {issuer: ISSUER, audience: "client"});
    return result.protectedHeader.kid;
}

(async () => {
    await cas.logg("Revoking previous keys so that a retry starts from the same key states");
    await cas.doRequest(`${ACTUATOR}/revoke`, "POST", {}, 200);
    assert.equal((await fetchKeys()).length, 4);
    const initial = await fetchKeyStates();
    assert.equal(initial.currentSigning.length, 1);
    assert.equal(initial.currentEncryption.length, 1);
    assert.equal(initial.futureSigning.length, 1);
    assert.equal(initial.futureEncryption.length, 1);
    assert.equal(initial.previous.length, 0);

    await cas.logg("ID tokens are signed with the current signing key and verify with the published keys");
    const idTokenBeforeRotation = await issueIdTokenSignedWith(initial.currentSigning[0]);
    assert.equal(await verifyWithPublishedKeys(idTokenBeforeRotation), initial.currentSigning[0]);

    await cas.logg("Rotating keys...");
    await cas.doRequest(`${ACTUATOR}/rotate`, "POST", {}, 200);
    const rotated = await fetchKeyStates();
    assert.deepEqual(rotated.currentSigning, initial.futureSigning, "The future signing key becomes current");
    assert.deepEqual(rotated.currentEncryption, initial.futureEncryption, "The future encryption key becomes current");
    assert.deepEqual([...rotated.previous].sort(), [...initial.currentSigning, ...initial.currentEncryption].sort(),
        "The current keys become previous");
    assert.equal(rotated.futureSigning.length, 1);
    assert.equal(rotated.futureEncryption.length, 1);
    const knownKeys = [...initial.currentSigning, ...initial.currentEncryption, ...initial.futureSigning, ...initial.futureEncryption];
    assert(!knownKeys.includes(rotated.futureSigning[0]), "A new future signing key is generated");
    assert(!knownKeys.includes(rotated.futureEncryption[0]), "A new future encryption key is generated");
    assert.equal((await fetchKeys()).length, 6);

    await cas.logg("The ID token signed before the rotation still verifies with the published previous key");
    assert.equal(await verifyWithPublishedKeys(idTokenBeforeRotation), initial.currentSigning[0]);

    await cas.logg("New ID tokens are signed with the new current key");
    const idTokenAfterRotation = await issueIdTokenSignedWith(rotated.currentSigning[0]);
    assert.equal(await verifyWithPublishedKeys(idTokenAfterRotation), rotated.currentSigning[0]);

    await cas.logg("Revoking keys...");
    await cas.doRequest(`${ACTUATOR}/revoke`, "POST", {}, 200);
    const revoked = await fetchKeyStates();
    assert.equal(revoked.previous.length, 0);
    assert.deepEqual(revoked.currentSigning, rotated.currentSigning);
    assert.deepEqual(revoked.futureSigning, rotated.futureSigning);
    const published = (await fetchKeys()).map((key) => key.kid);
    assert.equal(published.length, 4);
    assert(!published.includes(initial.currentSigning[0]), "The revoked signing key is no longer published");
    assert(!published.includes(initial.currentEncryption[0]), "The revoked encryption key is no longer published");

    await cas.logg("The ID token signed with the revoked key no longer verifies; the new one still does");
    await assert.rejects(verifyWithPublishedKeys(idTokenBeforeRotation), (error) => error.code === "ERR_JWKS_NO_MATCHING_KEY");
    assert.equal(await verifyWithPublishedKeys(idTokenAfterRotation), rotated.currentSigning[0]);
    assert.equal(jose.decodeProtectedHeader(await issueIdToken()).kid, rotated.currentSigning[0]);

    await cas.logg("Fetching all current keys...");
    await cas.doRequest(`${JWKS_ENDPOINT}?state=current`, "GET", {}, 200);
})();
