const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});

async function fetchNonce() {
    return cas.doPost("https://localhost:8443/cas/oidc/oidcVcNonce", "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.c_nonce !== undefined);
        return res.data.c_nonce;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function createProof(nonce) {
    const publicJwk = {
        kty: key.kty,
        n: key.n,
        e: key.e,
        kid: key.kid,
        use: key.use,
        alg: key.alg
    };
    return cas.createJwt({
        "jti": `${Date.now()}-${Math.random()}`,
        "iss": "client",
        "nonce": nonce,
        "aud": "https://localhost:8443/cas/oidc"
    }, privateKey, "RS256", {
        header: {
            typ: "openid4vci-proof+jwt",
            jwk: publicJwk
        }
    });
}

async function redeem(url) {
    return cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.c_nonce === undefined, "OpenID4VCI 1.0 moved the proof challenge to the nonce endpoint");
        return res.data.access_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function requestCredential(accessToken, nonce) {
    const credentialRequest = JSON.stringify({
        credential_configuration_id: "myorg",
        proofs: {
            jwt: [await createProof(nonce)]
        }
    });
    const result = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcCredential", "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
    }, 200, credentialRequest));
    assert(result.credentials.length === 1);
    return cas.decodeJwt(result.credentials[0].credential.split("~")[0]);
}

(async () => {
    const body = JSON.stringify({
        "principal": "casuser",
        "credentialConfigurationIds": ["myorg"]
    });
    const payload = JSON.parse(
        await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcCredentialOfferTransactions", "POST", {
            "Authorization": `Basic ${btoa("client:secret")}`,
            "Content-Length": body.length,
            "Content-Type": "application/json"
        }, 200, body)
    );
    assert(payload.transactionId !== undefined);
    assert(payload.txCode !== undefined);
    await cas.log(`Stateless transaction id is ${payload.transactionId.length} characters long`);

    const preAuthorizedCode = await cas.doGet(payload.credentialOfferUri,
        (res) => {
            assert(res.status === 200);
            assert(res.data.credential_configuration_ids[0] === "myorg");
            return res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"];
        }, (error) => {
            throw `Operation failed ${error}`;
        });
    assert(preAuthorizedCode !== undefined);

    const url = "https://localhost:8443/cas/oidc/token?grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code"
        + `&pre-authorized_code=${preAuthorizedCode}&tx_code=${payload.txCode}`;
    const accessToken = await redeem(url);
    await cas.log("Redeeming the pre-authorized code again; the stateless registry cannot make it single use");
    const secondAccessToken = await redeem(url);
    assert(secondAccessToken !== undefined);

    const nonce = await fetchNonce();
    const decoded = await requestCredential(accessToken, nonce);
    assert(decoded.sub === "casuser");
    assert(decoded.given_name === "CAS");
    assert(decoded.family_name === "User");
    assert(decoded.score === 95.5);
    assert(decoded.roles.includes("admin"));

    await cas.log("Using the same nonce again; the stateless registry cannot make it single use");
    const again = await requestCredential(secondAccessToken, nonce);
    assert(again.sub === "casuser");
})();
