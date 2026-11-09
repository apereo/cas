const fs = require("fs");
const path = require("path");
const https = require("https");
const crypto = require("crypto");
const assert = require("assert");
const express = require("express");
const jose = require("jose");
const cas = require("../../cas.js");
const {RELYING_PARTY_TRUST_MARK} = require("./trust-mark.js");

const ENTITY_ID = "https://localhost:9861";
const REDIRECT_URI = `${ENTITY_ID}/callback`;
const TRUST_ANCHOR = "https://localhost:8443/cas/oidc";
const ENTITY_STATEMENT_TYPE = "entity-statement+jwt";
const REQUEST_OBJECT_TYPE = "oauth-authz-req+jwt";
const PRIVATE_KEY_MEMBERS = ["d", "p", "q", "dp", "dq", "qi"];

const publicJwk = (jwk) => Object.fromEntries(Object.entries(jwk).filter(([name]) => !PRIVATE_KEY_MEMBERS.includes(name)));
const readJwk = (file) => JSON.parse(fs.readFileSync(file, "utf8")).keys[0];
const randomValue = () => crypto.randomBytes(24).toString("base64url");
const now = () => Math.floor(Date.now() / 1000);

/**
 * A minimal OpenID Federation relying party (OpenID Federation 1.0, section 12.1, automatic registration):
 * it publishes its entity configuration at its entity identifier, resolves the trust chain of the OP before it talks
 * to it, sends signed request objects carrying its trust chain, pushes authorization requests, and authenticates at
 * the token and PAR endpoints with private_key_jwt.
 */
class FederationRelyingParty {
    constructor(scenarioDirectory) {
        this.entityId = ENTITY_ID;
        this.redirectUri = REDIRECT_URI;
        this.federationJwk = readJwk(path.join(scenarioDirectory, "metadata", "rp-federation.jwks"));
        this.oidcJwk = readJwk(path.join(scenarioDirectory, "metadata", "rp-oidc.jwks"));
        this.trustAnchorJwksFile = path.join(scenarioDirectory, "metadata", "ta-federation.jwks");
        this.trustAnchorJwks = {keys: [publicJwk(readJwk(this.trustAnchorJwksFile))]};
        this.tlsDirectory = path.join(scenarioDirectory, "..", "..", "overlay", path.basename(scenarioDirectory));
    }

    async start() {
        this.federationKey = await jose.importJWK(this.federationJwk, "RS256");
        this.oidcKey = await jose.importJWK(this.oidcJwk, "RS256");
        this.trustMark = await cas.doRequest(`${TRUST_ANCHOR}/federation_trust_mark`, "POST",
            {"Content-Type": "application/x-www-form-urlencoded"}, 200, await this.trustMarkRequest());
        const app = express();
        app.get("/.well-known/openid-federation", async (req, res) => {
            res.type("application/entity-statement+jwt").send(await this.entityConfiguration());
        });
        app.get("/callback", (req, res) => res.type("text/plain").send(`Federation RP callback: ${JSON.stringify(req.query)}`));
        this.server = https.createServer({
            key: fs.readFileSync(path.join(this.tlsDirectory, "tls.key")),
            cert: fs.readFileSync(path.join(this.tlsDirectory, "tls.crt"))
        }, app);
        await new Promise((resolve) => this.server.listen(new URL(ENTITY_ID).port, resolve));
        await cas.log(`Federation relying party is listening at ${ENTITY_ID}`);
    }

    async stop() {
        if (this.server !== undefined) {
            await new Promise((resolve) => this.server.close(resolve));
        }
    }

    async entityConfiguration() {
        return new jose.SignJWT({
            iss: ENTITY_ID,
            sub: ENTITY_ID,
            iat: now(),
            exp: now() + 3600,
            jwks: {keys: [publicJwk(this.federationJwk)]},
            authority_hints: [TRUST_ANCHOR],
            trust_marks: [{trust_mark_type: RELYING_PARTY_TRUST_MARK, trust_mark: this.trustMark}],
            metadata: {
                openid_relying_party: {
                    client_name: "CAS federation scenario RP",
                    application_type: "web",
                    redirect_uris: [REDIRECT_URI],
                    response_types: ["code"],
                    grant_types: ["authorization_code"],
                    scope: "openid profile email",
                    client_registration_types: ["automatic"],
                    token_endpoint_auth_method: "private_key_jwt",
                    token_endpoint_auth_signing_alg: "RS256",
                    request_object_signing_alg: "RS256",
                    id_token_signed_response_alg: "RS256",
                    jwks: {keys: [publicJwk(this.oidcJwk)]}
                }
            }
        }).setProtectedHeader({alg: "RS256", kid: this.federationJwk.kid, typ: ENTITY_STATEMENT_TYPE}).sign(this.federationKey);
    }

    /**
     * Roll the key the relying party signs request objects and client assertions with over: the entity configuration
     * publishes only the new key from now on, while the OP holds the earlier one until it resolves the relying party
     * again.
     */
    async rolloverOidcKey() {
        const {privateKey} = await jose.generateKeyPair("RS256", {extractable: true});
        this.oidcJwk = {...await jose.exportJWK(privateKey), kid: `node-rp-oidc-${crypto.randomUUID()}`, use: "sig", alg: "RS256"};
        this.oidcKey = privateKey;
        await cas.log(`Relying party signing key rolled over to ${this.oidcJwk.kid}`);
    }

    /**
     * The body of a trust mark request authenticated with private_key_jwt, signed with the federation key that the
     * subordinate definition of this relying party at the trust anchor holds, and addressed only to the trust anchor.
     */
    async trustMarkRequest(subject = ENTITY_ID) {
        return new URLSearchParams({
            trust_mark_type: RELYING_PARTY_TRUST_MARK,
            sub: subject,
            ...await this.federationClientAuthentication()
        }).toString();
    }

    /**
     * private_key_jwt client authentication at a federation endpoint of the trust anchor (OpenID Federation 1.0,
     * section 8.8), signed with the federation key that the subordinate definition of this relying party holds and
     * addressed only to the trust anchor.
     */
    async federationClientAuthentication() {
        const clientAssertion = await new jose.SignJWT({
            iss: ENTITY_ID,
            sub: ENTITY_ID,
            aud: TRUST_ANCHOR,
            jti: crypto.randomUUID(),
            iat: now(),
            exp: now() + 60
        }).setProtectedHeader({alg: "RS256", kid: this.federationJwk.kid, typ: "JWT"}).sign(this.federationKey);
        return {
            client_assertion_type: "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            client_assertion: clientAssertion
        };
    }

    async fetchStatement(url) {
        const statement = await cas.doRequest(url, "GET", {}, 200);
        assert.equal(jose.decodeProtectedHeader(statement).typ, ENTITY_STATEMENT_TYPE);
        return statement;
    }

    async verifyStatement(statement, jwks, issuer, subject) {
        const {payload} = await jose.jwtVerify(statement, jose.createLocalJWKSet(jwks), {issuer, subject, typ: ENTITY_STATEMENT_TYPE});
        return payload;
    }

    async fetchSubordinateStatement(trustAnchor, subject) {
        const endpoint = trustAnchor.metadata.federation_entity.federation_fetch_endpoint;
        return this.fetchStatement(`${endpoint}?sub=${encodeURIComponent(subject)}`);
    }

    /**
     * Resolve an entity through the trust anchor whose keys are known out of band: trust anchor configuration,
     * the trust anchor's subordinate statement about the entity, and the entity's configuration verified with the keys
     * of that statement. Metadata in the subordinate statement takes precedence over the entity's own, per entity type.
     */
    async resolveEntity(entityId) {
        const trustAnchorStatement = await this.fetchStatement(`${TRUST_ANCHOR}/.well-known/openid-federation`);
        const trustAnchor = await this.verifyStatement(trustAnchorStatement, this.trustAnchorJwks, TRUST_ANCHOR, TRUST_ANCHOR);
        const entityStatement = await this.fetchStatement(`${entityId}/.well-known/openid-federation`);
        assert(jose.decodeJwt(entityStatement).authority_hints.includes(TRUST_ANCHOR));
        const subordinateStatement = await this.fetchSubordinateStatement(trustAnchor, entityId);
        const subordinate = await this.verifyStatement(subordinateStatement, trustAnchor.jwks, TRUST_ANCHOR, entityId);
        const entity = await this.verifyStatement(entityStatement, subordinate.jwks, entityId, entityId);
        const metadata = {};
        Object.keys(entity.metadata).forEach((type) => {
            metadata[type] = {...entity.metadata[type], ...subordinate.metadata?.[type]};
        });
        await cas.log(`Resolved ${entityId} through ${TRUST_ANCHOR} as ${Object.keys(metadata)}`);
        return metadata;
    }

    async resolveOpenIdProvider(opEntityId) {
        const metadata = (await this.resolveEntity(opEntityId)).openid_provider;
        assert(metadata.client_registration_types_supported.includes("automatic"));
        return metadata;
    }

    async trustChain() {
        const trustAnchorStatement = await this.fetchStatement(`${TRUST_ANCHOR}/.well-known/openid-federation`);
        const trustAnchor = jose.decodeJwt(trustAnchorStatement);
        const subordinateStatement = await this.fetchSubordinateStatement(trustAnchor, ENTITY_ID);
        await this.verifyStatement(subordinateStatement, trustAnchor.jwks, TRUST_ANCHOR, ENTITY_ID);
        return [await this.entityConfiguration(), subordinateStatement, trustAnchorStatement];
    }

    /**
     * Build an authorization request whose parameters travel in a request object signed with the RP key of the
     * resolved metadata. Claims can be overridden or removed (undefined) to build invalid request objects.
     */
    async authorizationRequest(op, overrides = {}, options = {}) {
        const verifier = randomValue();
        const state = randomValue();
        const nonce = randomValue();
        const claims = {
            iss: ENTITY_ID,
            aud: op.issuer,
            client_id: ENTITY_ID,
            jti: crypto.randomUUID(),
            iat: now(),
            exp: now() + 60,
            response_type: "code",
            redirect_uri: REDIRECT_URI,
            scope: "openid profile email",
            state,
            nonce,
            code_challenge: crypto.createHash("sha256").update(verifier).digest("base64url"),
            code_challenge_method: "S256",
            ...overrides
        };
        Object.keys(claims).filter((name) => claims[name] === undefined).forEach((name) => delete claims[name]);
        const header = {alg: "RS256", kid: this.oidcJwk.kid, typ: REQUEST_OBJECT_TYPE};
        if (options.trustChain === true) {
            header.trust_chain = await this.trustChain();
        } else if (Array.isArray(options.trustChain)) {
            header.trust_chain = options.trustChain;
        }
        const signingKey = options.signingKey ?? this.oidcKey;
        const signedRequestObject = options.unsigned === true
            ? new jose.UnsecuredJWT(claims).encode()
            : await new jose.SignJWT(claims).setProtectedHeader(header).sign(signingKey);
        const requestObject = options.encrypted === true
            ? await this.encryptRequestObject(op, signedRequestObject)
            : signedRequestObject;
        const params = new URLSearchParams({
            client_id: ENTITY_ID,
            response_type: "code",
            redirect_uri: REDIRECT_URI,
            scope: "openid profile email",
            request: requestObject
        });
        return {url: `${op.authorization_endpoint}?${params}`, params, state, nonce, verifier, jti: claims.jti, requestObject};
    }

    /**
     * Encrypt the signed request object to the OP's encryption key from its JWKS (OpenID Connect Core 1.0,
     * section 6.3.1), as a nested JWT.
     */
    async encryptRequestObject(op, signedRequestObject) {
        const jwks = JSON.parse(await cas.doRequest(op.jwks_uri, "GET", {}, 200));
        const key = jwks.keys.find((candidate) => candidate.use === "enc" && candidate.kty === "RSA");
        assert(key !== undefined, "The OP publishes no RSA encryption key");
        return new jose.CompactEncrypt(new TextEncoder().encode(signedRequestObject))
            .setProtectedHeader({alg: "RSA-OAEP-256", enc: "A256GCM", cty: "JWT", kid: key.kid})
            .encrypt(await jose.importJWK(key, "RSA-OAEP-256"));
    }

    async clientAssertion(audience) {
        return new jose.SignJWT({iss: ENTITY_ID, sub: ENTITY_ID, aud: audience, jti: crypto.randomUUID(), iat: now(), exp: now() + 60})
            .setProtectedHeader({alg: "RS256", kid: this.oidcJwk.kid})
            .sign(this.oidcKey);
    }

    async pushAuthorizationRequest(op, options = {}) {
        const request = await this.authorizationRequest(op, {}, options);
        request.params.set("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        request.params.set("client_assertion", await this.clientAssertion(op.issuer));
        const response = JSON.parse(await cas.doRequest(op.pushed_authorization_request_endpoint, "POST",
            {"Content-Type": "application/x-www-form-urlencoded"}, 201, request.params.toString()));
        assert(response.request_uri !== undefined);
        const params = new URLSearchParams({client_id: ENTITY_ID, request_uri: response.request_uri});
        return {...request, url: `${op.authorization_endpoint}?${params}`};
    }

    async exchangeCode(op, code, verifier, assertionAudience) {
        const params = new URLSearchParams({
            grant_type: "authorization_code",
            code,
            redirect_uri: REDIRECT_URI,
            client_id: ENTITY_ID,
            code_verifier: verifier,
            client_assertion_type: "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            client_assertion: await this.clientAssertion(assertionAudience)
        });
        const response = JSON.parse(await cas.doRequest(op.token_endpoint, "POST",
            {"Content-Type": "application/x-www-form-urlencoded"}, 200, params.toString()));
        assert(response.access_token !== undefined);
        assert(response.id_token !== undefined);
        return response;
    }

    async verifyIdToken(op, idToken, nonce) {
        const jwks = JSON.parse(await cas.doRequest(op.jwks_uri, "GET", {}, 200));
        const {payload} = await jose.jwtVerify(idToken, jose.createLocalJWKSet(jwks), {issuer: op.issuer, audience: ENTITY_ID});
        assert.equal(payload.nonce, nonce);
        assert.equal(payload.sub, "casuser");
        await cas.log(`Verified ID token for ${payload.sub} issued by ${payload.iss} to ${payload.aud}`);
        return payload;
    }
}

module.exports = {FederationRelyingParty, ENTITY_ID, REDIRECT_URI};
