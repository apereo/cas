const crypto = require("crypto");
const assert = require("assert");
const jose = require("jose");
const cas = require("../../cas.js");

const CLIENT_ID_PREFIX = "openid_federation:";
const REQUEST_OBJECT_TYPE = "oauth-authz-req+jwt";
const PRE_AUTHORIZED_CODE_GRANT = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
const now = () => Math.floor(Date.now() / 1000);
const basic = (clientId, secret) => `Basic ${Buffer.from(`${clientId}:${secret}`).toString("base64")}`;

/**
 * A minimal wallet that trusts verifiers through OpenID Federation (OpenID4VP 1.0 section 5.9.3, OpenID Federation for
 * Wallet Architectures section 7.1): it obtains a dc+sd-jwt credential from the CAS credential issuer with a
 * pre-authorized code, and presents it to a verifier whose client identifier carries the openid_federation prefix,
 * taking the verifier's keys and response URIs only from the openid_credential_verifier metadata resolved through the
 * trust anchor. The request object must be signed with one of those keys, and the response is encrypted to one of them.
 */
class FederationWallet {
    constructor(resolver, issuer, client) {
        this.resolver = resolver;
        this.issuer = issuer;
        this.client = client;
    }

    async issueCredential(configurationId) {
        const holder = await jose.generateKeyPair("ES256", {extractable: true});
        this.holderKey = holder.privateKey;
        const holderJwk = await jose.exportJWK(holder.publicKey);
        const authorization = basic(this.client.id, this.client.secret);

        const body = JSON.stringify({principal: "casuser", credentialConfigurationIds: [configurationId]});
        const transaction = JSON.parse(await cas.doRequest(`${this.issuer}/oidcVcCredentialOfferTransactions?scope=openid`, "POST", {
            "Authorization": authorization,
            "Content-Length": body.length,
            "Content-Type": "application/json"
        }, 200, body));
        const offer = JSON.parse(await cas.doRequest(transaction.credentialOfferUri, "GET", {}, 200));
        assert.equal(offer.credential_issuer, this.issuer);
        const preAuthorizedCode = offer.grants[PRE_AUTHORIZED_CODE_GRANT]["pre-authorized_code"];

        const tokenParams = new URLSearchParams({
            "grant_type": PRE_AUTHORIZED_CODE_GRANT,
            "pre-authorized_code": preAuthorizedCode,
            "tx_code": transaction.txCode,
            "scope": "openid"
        });
        const token = JSON.parse(await cas.doRequest(`${this.issuer}/token?${tokenParams}`, "POST", {
            "Authorization": authorization,
            "Content-Type": "application/json"
        }, 200, ""));
        assert(token.access_token !== undefined);

        const nonce = JSON.parse(await cas.doRequest(`${this.issuer}/oidcVcNonce`, "POST",
            {"Content-Type": "application/json"}, 200, "")).c_nonce;
        const proof = await new jose.SignJWT({aud: this.issuer, nonce, iat: now()})
            .setProtectedHeader({alg: "ES256", typ: "openid4vci-proof+jwt", jwk: holderJwk})
            .sign(this.holderKey);
        const credentialRequest = JSON.stringify({credential_configuration_id: configurationId, proofs: {jwt: [proof]}});
        const response = JSON.parse(await cas.doRequest(`${this.issuer}/oidcVcCredential`, "POST", {
            "Authorization": `Bearer ${token.access_token}`,
            "Content-Length": credentialRequest.length,
            "Content-Type": "application/json"
        }, 200, credentialRequest));
        const credential = response.credentials[0].credential;
        this.credential = credential.endsWith("~") ? credential : `${credential}~`;
        await cas.log(`Wallet holds a ${jose.decodeJwt(this.credential.split("~")[0]).vct} credential`);
        return this.credential;
    }

    /**
     * Resolve the verifier named by the client identifier and verify the request object it serves by reference
     * with the signing keys of its federation metadata.
     */
    async verifyRequest(authorizationRequest) {
        const parameters = new URL(authorizationRequest).searchParams;
        const clientId = parameters.get("client_id");
        assert(clientId.startsWith(CLIENT_ID_PREFIX), clientId);
        const entityId = clientId.substring(CLIENT_ID_PREFIX.length);
        const verifier = (await this.resolver.resolveEntity(entityId)).openid_credential_verifier;
        assert(verifier !== undefined, "The verifier publishes no openid_credential_verifier metadata");
        assert(verifier.jwks.keys.every((key) => key.kid !== undefined && key.d === undefined));

        const requestObject = await cas.doRequest(parameters.get("request_uri"), "GET", {"Accept": "application/oauth-authz-req+jwt"}, 200);
        const header = jose.decodeProtectedHeader(requestObject);
        assert.equal(header.x5c, undefined);
        const signingKeys = {keys: verifier.jwks.keys.filter((key) => key.use === "sig")};
        const {payload} = await jose.jwtVerify(requestObject, jose.createLocalJWKSet(signingKeys),
            {issuer: clientId, typ: REQUEST_OBJECT_TYPE});
        assert.equal(payload.client_id, clientId);
        assert.equal(payload.client_metadata, undefined);
        assert(verifier.response_uris.includes(payload.response_uri), `Untrusted response URI ${payload.response_uri}`);
        await cas.log(`Wallet verified the request object of ${entityId} with key ${header.kid} from its federation metadata`);
        return {verifier, request: payload};
    }

    async present(authorizationRequest) {
        const {verifier, request} = await this.verifyRequest(authorizationRequest);
        assert.equal(request.response_mode, "direct_post.jwt");
        const sdHash = crypto.createHash("sha256").update(this.credential).digest("base64url");
        const keyBinding = await new jose.SignJWT({aud: request.client_id, nonce: request.nonce, iat: now(), sd_hash: sdHash})
            .setProtectedHeader({alg: "ES256", typ: "kb+jwt"})
            .sign(this.holderKey);
        const vpToken = {[request.dcql_query.credentials[0].id]: [`${this.credential}${keyBinding}`]};

        const encryptionKey = verifier.jwks.keys.find((key) => key.use === "enc");
        assert(encryptionKey !== undefined, "The verifier publishes no encryption key");
        assert(verifier.encrypted_response_enc_values_supported.includes("A256GCM"));
        const response = await new jose.CompactEncrypt(Buffer.from(JSON.stringify({vp_token: vpToken, state: request.state})))
            .setProtectedHeader({alg: encryptionKey.alg, enc: "A256GCM", kid: encryptionKey.kid})
            .encrypt(await jose.importJWK(encryptionKey, encryptionKey.alg));
        const body = new URLSearchParams({response}).toString();
        const result = JSON.parse(await cas.doRequest(request.response_uri, "POST", {
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": body.length
        }, 200, body));
        assert.equal(result.status, "verified");
        await cas.log(`Wallet presented its credential, encrypted with ${encryptionKey.alg} to key ${encryptionKey.kid}`);
        return result;
    }
}

module.exports = {FederationWallet};
