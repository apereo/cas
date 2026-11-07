const cas = require("../../cas.js");
const assert = require("assert");
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");
const jose = require("jose");

const ISSUER = "https://localhost:8443/cas/oidc";
const TOKEN_ENDPOINT = `${ISSUER}/token`;
const PAR_ENDPOINT = `${ISSUER}/oidcPushAuthorize`;
const PROFILE_ENDPOINT = `${ISSUER}/profile`;
const CHALLENGE_ENDPOINT = `${ISSUER}/oidcAttestationChallenge`;
const CHALLENGE_TIME_TO_LIVE_SECONDS = 20;
const ATTESTATION_DIR = path.join(__dirname, "..", "..", "overlay", path.basename(__dirname));

async function sendPushAuthorizationRequest(redirectUrl) {
    const parameters = "response_type=code&"
        + `client_id=client&scope=${encodeURIComponent("openid profile MyCustomScope")}&`
        + `redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&`
        + "state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential"
        + "%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D&"
        + "client_secret=secret";

    return cas.doPost(`https://localhost:8443/cas/oidc/oidcPushAuthorize?${parameters}`, "",
        {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => res.data.request_uri,
        (error) => {
            cas.logr(`Operation failed to obtain request_uri: ${error}`);
            return undefined;
        }
    );
}

async function verifyPushAuthorizationRequestSuccess() {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const requestUri = await sendPushAuthorizationRequest(redirectUrl);
    assert(requestUri !== undefined && requestUri !== null && requestUri.length > 0, "Request URI is missing");

    await cas.sleep(3000);
    await cas.log(`Request URI ${requestUri}`);
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code&client_id=client&request_uri=${requestUri}`;
    await cas.goto(page, url);
    await cas.sleep(3000);
    await cas.loginWith(page);
    await cas.sleep(3000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["sub"] === "casuser");
    assert(decoded["client_id"] === "client");
    assert(decoded["preferred_username"] === "casuser");

    assert(decoded["identity-name"] === undefined);
    assert(decoded["common-name"] === undefined);
    assert(decoded["lastname"] === undefined);

    assert(decoded["cn"] !== undefined);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["name"] !== undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(decoded["common-name"] === undefined);
        assert(decoded["lastname"] === undefined);

        assert(res.data["cn"] !== undefined);
        assert(res.data["name"] !== undefined);
        assert(res.data["family_name"] !== undefined);
        assert(res.data.sub !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
    await cas.closeBrowser(browser);
}

async function verifyPushAuthorizationFailure() {
    await cas.log("Attempting push authorization request with invalid redirect URI...");
    const result = await sendPushAuthorizationRequest("https://apereo.github.io");
    if (result !== undefined) {
        throw "Operation should have failed to obtain request_uri with invalid redirect URI";
    }
}

async function post(url, params, headers = {}) {
    return cas.doPost(url, params, {
        "Content-Type": "application/x-www-form-urlencoded",
        ...headers
    }, (res) => res, (error) => {
        if (error.response === undefined) {
            throw error;
        }
        return error.response;
    });
}

async function loadAttester(name) {
    const certificate = new crypto.X509Certificate(fs.readFileSync(path.join(ATTESTATION_DIR, `${name}.pem`)));
    const pkcs8 = crypto.createPrivateKey(fs.readFileSync(path.join(ATTESTATION_DIR, `${name}.key`)))
        .export({type: "pkcs8", format: "pem"});
    return {
        x5c: [certificate.raw.toString("base64")],
        privateKey: await jose.importPKCS8(pkcs8, "ES256")
    };
}

async function newInstanceKey() {
    const {publicKey, privateKey} = await jose.generateKeyPair("ES256", {extractable: true});
    const publicJwk = await jose.exportJWK(publicKey);
    return {
        publicJwk,
        privateKey,
        thumbprint: await jose.calculateJwkThumbprint(publicJwk, "sha256")
    };
}

async function createClientAttestation(attester, clientId, instanceKey) {
    return new jose.SignJWT({cnf: {jwk: instanceKey.publicJwk}})
        .setProtectedHeader({alg: "ES256", typ: "oauth-client-attestation+jwt", x5c: attester.x5c})
        .setIssuer("https://attester.example.org")
        .setSubject(clientId)
        .setIssuedAt()
        .setExpirationTime("10m")
        .sign(attester.privateKey);
}

async function createProofOfPossession(signingKey, clientId, challenge = undefined) {
    const claims = challenge === undefined ? {} : {challenge};
    return new jose.SignJWT(claims)
        .setProtectedHeader({alg: "ES256", typ: "oauth-client-attestation-pop+jwt"})
        .setIssuer(clientId)
        .setAudience(ISSUER)
        .setJti(await cas.randomWord(16, false))
        .setIssuedAt()
        .sign(signingKey);
}

async function createDPoPProof(instanceKey, url, accessToken = undefined) {
    const claims = {htm: "POST", htu: url};
    if (accessToken !== undefined) {
        claims.ath = crypto.createHash("sha256").update(accessToken).digest("base64url");
    }
    return new jose.SignJWT(claims)
        .setProtectedHeader({alg: "ES256", typ: "dpop+jwt", jwk: instanceKey.publicJwk})
        .setJti(await cas.randomWord(16, false))
        .setIssuedAt()
        .sign(instanceKey.privateKey);
}

async function fetchAttestationChallenge() {
    const response = await post(CHALLENGE_ENDPOINT, "");
    assert.equal(response.status, 200);
    assert(response.headers["cache-control"].includes("no-store"), response.headers["cache-control"]);
    const challenge = response.data.attestation_challenge;
    assert(challenge !== undefined && challenge.length > 0, "Attestation challenge is missing");
    return challenge;
}

async function attestationHeaders(attestation, instanceKey, clientId, challenge = undefined) {
    return {
        "OAuth-Client-Attestation": attestation,
        "OAuth-Client-Attestation-PoP": await createProofOfPossession(instanceKey.privateKey, clientId, challenge)
    };
}

async function assertClientAuthenticationRefused(response, label) {
    assert.equal(response.status, 401, `${label}: expected 401 but got ${response.status}`);
    assert(response.headers["www-authenticate"] !== undefined, `${label}: missing WWW-Authenticate`);
    await cas.logg(`${label}: client authentication is refused`);
}

async function assertAttestationChallengeRequired(response, label) {
    assert.equal(response.status, 400, `${label}: expected 400 but got ${response.status}`);
    assert.equal(response.data.error, "use_attestation_challenge");
    const challenge = response.headers["oauth-client-attestation-challenge"];
    assert(challenge !== undefined && challenge.length > 0, `${label}: missing OAuth-Client-Attestation-Challenge header`);
    assert(response.headers["cache-control"].includes("no-store"), response.headers["cache-control"]);
    await cas.logg(`${label}: CAS asks for a challenge and hands out ${challenge}`);
    return challenge;
}

function clientCredentialsRequest(clientId) {
    return new URLSearchParams({grant_type: "client_credentials", client_id: clientId});
}

async function verifyClientAttestationDiscovery() {
    await cas.log("Discovery advertises attestation-based client authentication and the challenge endpoint");
    await cas.doGet(`${ISSUER}/.well-known/openid-configuration`, (res) => {
        const methods = res.data.token_endpoint_auth_methods_supported;
        assert(methods.includes("attest_jwt_client_auth"), methods);
        assert(methods.includes("attest_jwt_client_auth_dpop"), methods);
        assert.equal(res.data.challenge_endpoint, CHALLENGE_ENDPOINT);
        assert(res.data.client_attestation_signing_alg_values_supported.includes("ES256"));
        assert(res.data.client_attestation_pop_signing_alg_values_supported.includes("ES256"));
    }, (error) => {
        throw `Unable to fetch discovery document: ${error}`;
    });
}

async function verifyClientAttestationAtTokenEndpoint(attester) {
    const clientId = "attested";
    const instanceKey = await newInstanceKey();
    const attestation = await createClientAttestation(attester, clientId, instanceKey);

    await cas.log("A proof of possession without a challenge is answered with a fresh challenge");
    let response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId),
        await attestationHeaders(attestation, instanceKey, clientId));
    const challenge = await assertAttestationChallengeRequired(response, "No challenge");

    await cas.log("A challenge that CAS never handed out is refused the same way");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId),
        await attestationHeaders(attestation, instanceKey, clientId, "TST-unknown-attestation-challenge"));
    await assertAttestationChallengeRequired(response, "Unknown challenge");

    await cas.log("The challenge from the error response authenticates the client");
    const headers = await attestationHeaders(attestation, instanceKey, clientId, challenge);
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), headers);
    assert.equal(response.status, 200);
    assert(response.data.access_token !== undefined);
    assert.equal(response.data.token_type, "Bearer");

    await cas.log("The same proof of possession cannot be used twice");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), headers);
    await assertClientAuthenticationRefused(response, "Replayed proof of possession");

    await cas.log("A proof of possession signed with a key other than the attested one is refused");
    const otherKey = await newInstanceKey();
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation": attestation,
        "OAuth-Client-Attestation-PoP": await createProofOfPossession(otherKey.privateKey, clientId, await fetchAttestationChallenge())
    });
    await assertClientAuthenticationRefused(response, "Proof of possession with the wrong key");

    await cas.log("A client attestation signed by a self-signed attester is refused");
    const rogueAttestation = await createClientAttestation(await loadAttester("rogue"), clientId, instanceKey);
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId),
        await attestationHeaders(rogueAttestation, instanceKey, clientId, await fetchAttestationChallenge()));
    await assertClientAuthenticationRefused(response, "Self-signed attester");

    await cas.log("A client attestation for another client than the request names is refused");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest("client"),
        await attestationHeaders(attestation, instanceKey, clientId, await fetchAttestationChallenge()));
    await assertClientAuthenticationRefused(response, "Attestation sub is not the client_id");

    await cas.log("A proof of possession without its client attestation is refused");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation-PoP": await createProofOfPossession(instanceKey.privateKey, clientId, await fetchAttestationChallenge())
    });
    await assertClientAuthenticationRefused(response, "Missing client attestation");

    await cas.log("The client registered attest_jwt_client_auth, so the DPoP combined mode is refused");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation": attestation,
        "DPoP": await createDPoPProof(instanceKey, TOKEN_ENDPOINT)
    });
    await assertClientAuthenticationRefused(response, "DPoP combined mode for an attest_jwt_client_auth client");

    await cas.log(`A challenge is refused once it is older than ${CHALLENGE_TIME_TO_LIVE_SECONDS} seconds`);
    const expiringChallenge = await fetchAttestationChallenge();
    await cas.sleep((CHALLENGE_TIME_TO_LIVE_SECONDS + 2) * 1000);
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId),
        await attestationHeaders(attestation, instanceKey, clientId, expiringChallenge));
    await assertAttestationChallengeRequired(response, "Expired challenge");
}

async function verifyClientAttestationDPoPAtTokenEndpoint(attester) {
    const clientId = "attested-dpop";
    const instanceKey = await newInstanceKey();
    const attestation = await createClientAttestation(attester, clientId, instanceKey);

    await cas.log("In the DPoP combined mode a DPoP proof made with the attested key stands for the proof of possession");
    const dpopProof = await createDPoPProof(instanceKey, TOKEN_ENDPOINT);
    let response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation": attestation,
        "DPoP": dpopProof
    });
    assert.equal(response.status, 200);
    assert(response.data.access_token !== undefined);
    assert.equal(response.data.token_type, "DPoP");

    await cas.log("The same DPoP proof cannot authenticate the client twice");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation": attestation,
        "DPoP": dpopProof
    });
    await assertClientAuthenticationRefused(response, "Replayed DPoP proof");

    await cas.log("A DPoP proof made with a key other than the attested one is refused");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId), {
        "OAuth-Client-Attestation": attestation,
        "DPoP": await createDPoPProof(await newInstanceKey(), TOKEN_ENDPOINT)
    });
    await assertClientAuthenticationRefused(response, "DPoP proof with the wrong key");

    await cas.log("The client registered attest_jwt_client_auth_dpop, so a separate proof of possession is refused");
    response = await post(TOKEN_ENDPOINT, clientCredentialsRequest(clientId),
        await attestationHeaders(attestation, instanceKey, clientId, await fetchAttestationChallenge()));
    await assertClientAuthenticationRefused(response, "Proof of possession for an attest_jwt_client_auth_dpop client");
}

async function pushAuthorizationRequest(clientId, redirectUrl, headers) {
    const parameters = new URLSearchParams({
        response_type: "code",
        client_id: clientId,
        scope: "openid profile",
        redirect_uri: redirectUrl,
        nonce: await cas.randomWord(12, false),
        state: await cas.randomWord(12, false)
    });
    return post(PAR_ENDPOINT, parameters, headers);
}

async function authorizeWithRequestUri(page, clientId, requestUri) {
    await cas.goto(page, `${ISSUER}/oidcAuthorize?response_type=code&client_id=${clientId}&request_uri=${encodeURIComponent(requestUri)}`);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(2000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    return cas.assertParameter(page, "code");
}

function authorizationCodeRequest(clientId, redirectUrl, code) {
    return new URLSearchParams({
        grant_type: "authorization_code",
        client_id: clientId,
        redirect_uri: redirectUrl,
        code
    });
}

async function verifyClientAttestationWithPushedAuthorization(attester) {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const clientId = "attested";
    const redirectUrl = "https://localhost:9859/anything/attested";
    const instanceKey = await newInstanceKey();
    const attestation = await createClientAttestation(attester, clientId, instanceKey);

    await cas.log("A pushed authorization request without client authentication is refused");
    let response = await pushAuthorizationRequest(clientId, redirectUrl, {});
    await assertClientAuthenticationRefused(response, "Pushed authorization request without attestation");

    await cas.log("A pushed authorization request for another client than the attestation names is refused");
    response = await pushAuthorizationRequest("client", redirectUrl,
        await attestationHeaders(attestation, instanceKey, clientId, await fetchAttestationChallenge()));
    await assertClientAuthenticationRefused(response, "Pushed authorization request with attestation sub not the client_id");

    await cas.log("A pushed authorization request authenticated by client attestation gets a request_uri");
    response = await pushAuthorizationRequest(clientId, redirectUrl,
        await attestationHeaders(attestation, instanceKey, clientId, await fetchAttestationChallenge()));
    assert(response.status === 200 || response.status === 201, `Unexpected status ${response.status}`);
    const requestUri = response.data.request_uri;
    assert(requestUri !== undefined && requestUri.length > 0, "Request URI is missing");

    const code = await authorizeWithRequestUri(page, clientId, requestUri);
    await cas.log("The authorization code is redeemed with a new client attestation proof of possession");
    response = await post(TOKEN_ENDPOINT, authorizationCodeRequest(clientId, redirectUrl, code),
        await attestationHeaders(attestation, instanceKey, clientId, await fetchAttestationChallenge()));
    assert.equal(response.status, 200);
    assert(response.data.access_token !== undefined);
    assert.equal(response.data.token_type, "Bearer");
    const idToken = await cas.decodeJwt(response.data.id_token);
    assert.equal(idToken.sub, "casuser");
    assert.equal(idToken.aud, clientId);

    await cas.log("In the DPoP combined mode, the pushed authorization request binds the code to the attested key");
    const dpopClientId = "attested-dpop";
    const dpopRedirectUrl = "https://localhost:9859/anything/dpopattested";
    const dpopInstanceKey = await newInstanceKey();
    const dpopAttestation = await createClientAttestation(attester, dpopClientId, dpopInstanceKey);
    response = await pushAuthorizationRequest(dpopClientId, dpopRedirectUrl, {
        "OAuth-Client-Attestation": dpopAttestation,
        "DPoP": await createDPoPProof(dpopInstanceKey, PAR_ENDPOINT)
    });
    assert(response.status === 200 || response.status === 201, `Unexpected status ${response.status}`);
    const dpopRequestUri = response.data.request_uri;
    assert(dpopRequestUri !== undefined && dpopRequestUri.length > 0, "Request URI is missing");

    const dpopCode = await authorizeWithRequestUri(page, dpopClientId, dpopRequestUri);
    response = await post(TOKEN_ENDPOINT, authorizationCodeRequest(dpopClientId, dpopRedirectUrl, dpopCode), {
        "OAuth-Client-Attestation": dpopAttestation,
        "DPoP": await createDPoPProof(dpopInstanceKey, TOKEN_ENDPOINT)
    });
    assert.equal(response.status, 200);
    assert.equal(response.data.token_type, "DPoP");
    const accessToken = response.data.access_token;
    assert(accessToken !== undefined);

    await cas.log("The access token is bound to the attested key and works at the profile endpoint with a DPoP proof");
    response = await post(PROFILE_ENDPOINT, "", {
        "Authorization": `DPoP ${accessToken}`,
        "DPoP": await createDPoPProof(dpopInstanceKey, PROFILE_ENDPOINT, accessToken)
    });
    assert.equal(response.status, 200);
    assert.equal(response.data.sub, "casuser");

    await cas.closeBrowser(browser);
}

async function verifyClientAttestation() {
    const attester = await loadAttester("attester");
    await verifyClientAttestationDiscovery();
    await verifyClientAttestationAtTokenEndpoint(attester);
    await verifyClientAttestationDPoPAtTokenEndpoint(attester);
    await verifyClientAttestationWithPushedAuthorization(attester);
}

(async () => {
    await verifyPushAuthorizationRequestSuccess();
    await verifyPushAuthorizationFailure();
    await verifyClientAttestation();
})();
