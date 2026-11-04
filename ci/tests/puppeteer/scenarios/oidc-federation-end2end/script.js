const fs = require("fs");
const path = require("path");
const assert = require("assert");
const crypto = require("crypto");
const jose = require("jose");
const cas = require("../../cas.js");
const {FederationRelyingParty, ENTITY_ID, REDIRECT_URI} = require("./rp.js");
const {RELYING_PARTY_TRUST_MARK, issueRelyingPartyTrustMark} = require("./trust-mark.js");
const {FederationWallet} = require("./wallet.js");

const OP_ENTITY_ID = "https://localhost:8444/cas/oidc";
const OFFA_ENTITY_ID = "https://localhost";
const TRUST_ANCHOR = "https://localhost:8443/cas/oidc";
const VC_CLIENT = {id: "vc-client", secret: "vc-secret"};
const VC_CLIENT_AUTHORIZATION = `Basic ${Buffer.from(`${VC_CLIENT.id}:${VC_CLIENT.secret}`).toString("base64")}`;

async function approveConsent(page) {
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
}

async function loginIfNeeded(page) {
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(2000);
    }
}

async function verifyOffaLogin(page) {
    await cas.log("go-oidfed OFFA: automatic registration with a request object signed by OFFA");
    const next = `${OFFA_ENTITY_ID}/`;
    await cas.goto(page, `${OFFA_ENTITY_ID}/login?op=${encodeURIComponent(OP_ENTITY_ID)}&next=${encodeURIComponent(next)}`);
    await cas.sleep(2000);
    await loginIfNeeded(page);
    await approveConsent(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrl(page, next);
    const body = await cas.innerText(page, "body");
    assert(body.includes("casuser"), body);
    assert(body.includes(OP_ENTITY_ID), body);
}

/**
 * OFFA discovers the OPs of its trust anchor by walking the trust anchor's subordinate listing, refreshing every ten
 * seconds; its first attempt runs before the trust anchor is up, so the login page is reloaded until the OP shows.
 */
async function verifyOffaDiscovery(page) {
    await cas.log("go-oidfed OFFA: OP discovery through the trust anchor's subordinate listing");
    let html = "";
    for (let attempt = 0; attempt < 10 && !html.includes(`data-value="${OP_ENTITY_ID}"`); attempt++) {
        await cas.goto(page, `${OFFA_ENTITY_ID}/login`);
        await cas.sleep(1000);
        html = await page.content();
        if (!html.includes(`data-value="${OP_ENTITY_ID}"`)) {
            await cas.sleep(4000);
        }
    }
    assert(html.includes(`data-value="${OP_ENTITY_ID}"`), "OFFA did not discover the CAS OP");
}

/**
 * The trust anchor names the issuers it accepts for each trust mark type of the federation; the OP accepts the relying
 * party trust marks that OFFA and the Node RP carry because their issuer is listed here.
 */
async function verifyTrustMarkIssuers() {
    await cas.log("Trust anchor: trust mark issuers in its entity configuration");
    const trustAnchorClaims = jose.decodeJwt(await cas.doRequest(`${TRUST_ANCHOR}/.well-known/openid-federation`, "GET", {}, 200));
    assert.deepEqual(trustAnchorClaims.trust_mark_issuers, {
        [`${TRUST_ANCHOR}/trust-marks/relying-party`]: [TRUST_ANCHOR],
        [`${TRUST_ANCHOR}/trust-marks/identity-provider`]: [TRUST_ANCHOR]
    });
}

/**
 * The trust anchor is also the issuer of the federation's trust marks (OpenID Federation 1.0, sections 8.4 to 8.6): it
 * publishes its trust mark endpoints, lists the entities it issues relying party trust marks to, issued the Node RP its
 * trust mark, and answers the status of a trust mark: active for an entity it issues the type to, revoked for one it
 * no longer does, and not found for an entity it never issued it to. The status response verifies with its key.
 */
async function verifyTrustMarkIssuance(rp) {
    await cas.log("Trust anchor: trust mark issuer endpoints");
    const federationEntity = jose.decodeJwt(await cas.doRequest(`${TRUST_ANCHOR}/.well-known/openid-federation`, "GET", {}, 200))
        .metadata.federation_entity;
    assert.equal(federationEntity.federation_trust_mark_endpoint, `${TRUST_ANCHOR}/federation_trust_mark`);
    assert.equal(federationEntity.federation_trust_mark_list_endpoint, `${TRUST_ANCHOR}/federation_trust_mark_list`);
    const statusEndpoint = federationEntity.federation_trust_mark_status_endpoint;
    assert.equal(statusEndpoint, `${TRUST_ANCHOR}/federation_trust_mark_status`);

    const listing = JSON.parse(await cas.doRequest(`${federationEntity.federation_trust_mark_list_endpoint}?${
        new URLSearchParams({trust_mark_type: RELYING_PARTY_TRUST_MARK})}`, "GET", {}, 200));
    assert.deepEqual(listing.sort(), [ENTITY_ID, OFFA_ENTITY_ID].sort());

    const issued = jose.decodeJwt(rp.trustMark);
    assert.equal(issued.iss, TRUST_ANCHOR);
    assert.equal(issued.sub, ENTITY_ID);
    assert.equal(issued.trust_mark_type, RELYING_PARTY_TRUST_MARK);
    assert.equal(jose.decodeProtectedHeader(rp.trustMark).typ, "trust-mark+jwt");

    const trustAnchorKey = JSON.parse(fs.readFileSync(path.join(__dirname, "metadata", "ta-federation.jwks"), "utf8")).keys[0];
    const publicKey = await jose.importJWK(Object.fromEntries(Object.entries(trustAnchorKey)
        .filter(([name]) => !["d", "p", "q", "dp", "dq", "qi"].includes(name))), "RS256");
    const statusOf = async (trustMark) => {
        const response = await cas.doRequest(statusEndpoint, "POST", {"Content-Type": "application/x-www-form-urlencoded"}, 200,
            new URLSearchParams({trust_mark: trustMark}).toString());
        const {payload} = await jose.jwtVerify(response, publicKey, {issuer: TRUST_ANCHOR, typ: "trust-mark-status-response+jwt"});
        assert.equal(payload.trust_mark, trustMark);
        return payload.status;
    };
    assert.equal(await statusOf(rp.trustMark), "active");
    const revoked = await issueRelyingPartyTrustMark(path.join(__dirname, "metadata", "ta-federation.jwks"), "https://localhost:9862");
    assert.equal(await statusOf(revoked), "revoked");

    await cas.doRequest(`${federationEntity.federation_trust_mark_endpoint}?${
        new URLSearchParams({trust_mark_type: RELYING_PARTY_TRUST_MARK, sub: "https://localhost:9862"})}`, "GET", {}, 404);
}

async function verifySubordinateListing() {
    await cas.log("Trust anchor: subordinate listing filtered by entity type");
    const listEndpoint = `${TRUST_ANCHOR}/list`;
    const listing = async (query) => JSON.parse(await cas.doRequest(`${listEndpoint}${query}`, "GET", {}, 200));
    assert.deepEqual(await listing("?entity_type=openid_provider"), [OP_ENTITY_ID]);
    assert.deepEqual((await listing("?entity_type=openid_relying_party")).sort(), [OFFA_ENTITY_ID, ENTITY_ID].sort());
    assert.deepEqual(await listing("?intermediate=true"), []);
    assert.equal((await listing("")).length, 3);
    const unsupported = await cas.doRequest(`${listEndpoint}?trust_marked=true`, "GET", {}, 400);
    assert.equal(JSON.parse(unsupported).error, "unsupported_parameter");
}

/**
 * The OP's entity configuration also describes it as the credential issuer it is with the verifiable credentials
 * module, and as the authorization server the credential issuer names, and carries the trust mark the trust anchor
 * issued to it, which verifies with the trust anchor's federation key.
 */
async function verifyOpEntityConfiguration() {
    await cas.log("OP: credential issuer metadata and trust marks in the entity configuration");
    const entityConfiguration = await cas.doRequest(`${OP_ENTITY_ID}/.well-known/openid-federation`, "GET", {}, 200);
    const claims = jose.decodeJwt(entityConfiguration);
    const credentialIssuer = claims.metadata.openid_credential_issuer;
    assert.equal(credentialIssuer.credential_issuer, OP_ENTITY_ID);
    assert.deepEqual(credentialIssuer.authorization_servers, [OP_ENTITY_ID]);
    assert(credentialIssuer.credential_configurations_supported.member !== undefined);
    assert.equal(claims.metadata.oauth_authorization_server.issuer, OP_ENTITY_ID);
    const verifier = claims.metadata.openid_credential_verifier;
    assert.deepEqual(verifier.response_uris, [`${OP_ENTITY_ID}/oidcVcPresentationResponse`]);
    assert(verifier.vp_formats_supported["dc+sd-jwt"] !== undefined);
    assert(verifier.jwks.keys.some((key) => key.use === "sig" && key.kid !== undefined));
    assert(verifier.jwks.keys.some((key) => key.use === "enc" && key.alg === "RSA-OAEP-256"));
    assert(verifier.jwks.keys.every((key) => key.d === undefined));
    assert.equal(claims.metadata.openid_provider.issuer, OP_ENTITY_ID);

    assert.equal(claims.trust_marks.length, 1);
    const trustMarkType = `${TRUST_ANCHOR}/trust-marks/identity-provider`;
    assert.equal(claims.trust_marks[0].trust_mark_type, trustMarkType);
    const trustAnchorKey = JSON.parse(fs.readFileSync(path.join(__dirname, "metadata", "ta-federation.jwks"), "utf8")).keys[0];
    const publicKey = Object.fromEntries(Object.entries(trustAnchorKey)
        .filter(([name]) => !["d", "p", "q", "dp", "dq", "qi"].includes(name)));
    const {payload} = await jose.jwtVerify(claims.trust_marks[0].trust_mark, await jose.importJWK(publicKey, "RS256"), {
        issuer: TRUST_ANCHOR,
        subject: OP_ENTITY_ID,
        typ: "trust-mark+jwt"
    });
    assert.equal(payload.trust_mark_type, trustMarkType);
}

/**
 * CAS as an OpenID4VP verifier trusted through the federation: the scenario's wallet gets a credential from the CAS
 * credential issuer, resolves CAS through the trust anchor from the openid_federation client identifier, verifies the
 * request object with the keys of the openid_credential_verifier metadata, and presents the credential encrypted to the
 * key published there; the relying party then collects the verified claims.
 */
async function verifyFederatedPresentation(rp) {
    await cas.log("Wallet: presentation to CAS as an OpenID Federation verifier");
    const wallet = new FederationWallet(rp, OP_ENTITY_ID, VC_CLIENT);
    await wallet.issueCredential("member");

    const body = JSON.stringify({
        credentials: [{
            id: "member",
            format: "dc+sd-jwt",
            vct_values: [`${OP_ENTITY_ID}/oidcVcCredentialType/member`],
            claims: [{path: ["given_name"], required: true}]
        }]
    });
    const presentation = JSON.parse(await cas.doRequest(`${OP_ENTITY_ID}/oidcVcPresentationRequest`, "POST", {
        "Authorization": VC_CLIENT_AUTHORIZATION,
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 200, body));
    assert(presentation.request_uri !== undefined);
    const deepLink = new URL(presentation.authorization_request).searchParams;
    assert.equal(deepLink.get("client_id"), `openid_federation:${OP_ENTITY_ID}`);
    assert.equal(deepLink.get("request_uri"), presentation.request_uri);

    await wallet.present(presentation.authorization_request);
    const outcome = JSON.parse(await cas.doRequest(`${OP_ENTITY_ID}/oidcVcPresentationResult?requestId=${presentation.request_id}`,
        "GET", {"Authorization": VC_CLIENT_AUTHORIZATION}, 200));
    assert.equal(outcome.status, "verified");
    assert.equal(outcome.claims.member.given_name, "CAS");
}

async function completeAuthorization(page, rp, op, request, assertionAudience) {
    await cas.goto(page, request.url);
    await cas.sleep(2000);
    await loginIfNeeded(page);
    await approveConsent(page);
    await cas.sleep(1000);
    assert(page.url().startsWith(REDIRECT_URI), page.url());
    const code = await cas.assertParameter(page, "code");
    assert.equal(new URL(page.url()).searchParams.get("state"), request.state);
    const tokens = await rp.exchangeCode(op, code, request.verifier, assertionAudience);
    await rp.verifyIdToken(op, tokens.id_token, request.nonce);
}

async function assertRefused(url, description) {
    let status = 0;
    const body = await cas.doRequest(url, "GET", {}, 0, undefined, (res) => {
        status = res.statusCode;
    });
    await cas.log(`Refused with ${status}: ${body}`);
    assert.equal(status, 400);
    assert(body.includes("invalid_request"), body);
    if (description !== undefined) {
        assert(body.includes(description), body);
    }
}

async function assertUnknownClient(op, clientId) {
    const params = new URLSearchParams({client_id: clientId, response_type: "code", redirect_uri: REDIRECT_URI, scope: "openid"});
    let response = undefined;
    await cas.doRequest(`${op.authorization_endpoint}?${params}`, "GET", {}, 0, undefined, (res) => {
        response = res;
    });
    await cas.log(`Unknown client ${clientId} answered with ${response.statusCode}`);
    assert(response.statusCode >= 400, `Unexpected status ${response.statusCode}`);
    assert(!(response.headers.location ?? "").startsWith(REDIRECT_URI));
}

async function verifyRefusedRequests(rp, op, usedRequest) {
    await cas.log("Automatic registration requires proof of the relying party keys");
    const plain = new URLSearchParams({client_id: ENTITY_ID, response_type: "code", redirect_uri: REDIRECT_URI, scope: "openid"});
    await assertRefused(`${op.authorization_endpoint}?${plain}`,
        "Automatic registration requires a signed request object or a pushed authorization request");

    await assertRefused((await rp.authorizationRequest(op, {aud: "https://localhost:8443/cas/oidc"})).url);
    await assertRefused((await rp.authorizationRequest(op, {aud: [op.issuer, "https://localhost:8443/cas/oidc"]})).url);
    await assertRefused((await rp.authorizationRequest(op, {sub: ENTITY_ID})).url, "Request object must not carry sub");
    await assertRefused((await rp.authorizationRequest(op, {iss: OFFA_ENTITY_ID})).url);
    await assertRefused((await rp.authorizationRequest(op, {exp: Math.floor(Date.now() / 1000) - 600})).url);
    await assertRefused((await rp.authorizationRequest(op, {jti: undefined})).url, "Request object has no jti");
    await assertRefused((await rp.authorizationRequest(op, {}, {unsigned: true})).url, "Request object signature or claims are invalid");

    const {privateKey} = await jose.generateKeyPair("RS256");
    await assertRefused((await rp.authorizationRequest(op, {}, {signingKey: privateKey})).url,
        "Request object signature or claims are invalid");

    await assertRefused((await rp.authorizationRequest(op, {jti: usedRequest.jti, state: crypto.randomUUID()})).url,
        "Request object jti has already been used");

    await cas.log("Clients outside the federation are not registered");
    await assertUnknownClient(op, "https://localhost:9862");
    await assertUnknownClient(op, ENTITY_ID.replace("https://", "http://"));
}

/**
 * The same request object carrying its trust chain, pushed instead of passed by value; the client assertion is
 * addressed to the OP's entity identifier, as federation PAR requires. Passed by value, the request object travels
 * through the CAS login inside its service URL, which is longer than the 4096 characters CAS matches by default; the
 * OP of this scenario turns that check off (cas.service-registry.core.maximum-service-id-length=0).
 */
async function verifyPushedRequestWithTrustChain(page, rp, op) {
    await cas.log("Node RP: pushed request object carrying its trust chain, private_key_jwt addressed to the OP");
    const pushed = await rp.pushAuthorizationRequest(op, {trustChain: true});
    await completeAuthorization(page, rp, op, pushed, op.issuer);
}

/**
 * The relying party rolls its signing key over twice: the OP, holding the earlier key in its registration, resolves the
 * relying party again (OpenID Federation 1.0, section 12.3) the first time from a request object passed by value and the
 * second time from a pushed request and its client assertion, once the OP lets the entity be resolved again; neither is
 * refused, and the token requests authenticate with the new key.
 */
async function verifyKeyRollover(page, rp, op) {
    await cas.log("Node RP: signing key rolled over, request object passed by value");
    await rp.rolloverOidcKey();
    await completeAuthorization(page, rp, op, await rp.authorizationRequest(op), op.token_endpoint);

    await cas.sleep(6000);
    await cas.log("Node RP: signing key rolled over again, pushed request with private_key_jwt");
    await rp.rolloverOidcKey();
    await completeAuthorization(page, rp, op, await rp.pushAuthorizationRequest(op), op.issuer);
}

(async () => {
    const rp = new FederationRelyingParty(__dirname);
    await rp.start();
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await verifySubordinateListing();
        await verifyTrustMarkIssuers();
        await verifyTrustMarkIssuance(rp);
        await verifyOpEntityConfiguration();
        await verifyOffaDiscovery(page);
        await verifyOffaLogin(page);

        const op = await rp.resolveOpenIdProvider(OP_ENTITY_ID);
        await cas.log("Node RP: automatic registration with a request object carrying its trust chain, passed by value");
        const request = await rp.authorizationRequest(op, {}, {trustChain: true});
        await completeAuthorization(page, rp, op, request, op.token_endpoint);
        await verifyPushedRequestWithTrustChain(page, rp, op);
        await cas.log("Node RP: signed request object encrypted to the OP, passed by value");
        await completeAuthorization(page, rp, op, await rp.authorizationRequest(op, {}, {encrypted: true}), op.token_endpoint);
        await verifyKeyRollover(page, rp, op);

        await verifyRefusedRequests(rp, op, request);
        await verifyFederatedPresentation(rp);
    } finally {
        await cas.closeBrowser(browser);
        await rp.stop();
    }
})();
