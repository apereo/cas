const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});

async function createPublicKey() {
    const nonce = await cas.doPost("https://localhost:8443/cas/oidc/oidcVcNonce", "", {
        "Content-Type": "application/json"
    }, (res) => {
        cas.log(res.data);
        return res.data.c_nonce;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const publicJwk = {
        kty: key.kty,
        n: key.n,
        e: key.e,
        kid: key.kid,
        use: key.use,
        alg: key.alg
    };

    return cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
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

(async () => {
    await cas.doGet("https://localhost:8443/cas/oidc/.well-known/openid-credential-issuer",
        (res) => {
            assert(res.status === 200);
            assert(res.data.credential_issuer !== undefined);
            assert(res.data.authorization_servers !== undefined);
            assert(res.data.credential_endpoint !== undefined);
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    const cNonce = await cas.doPost("https://localhost:8443/cas/oidc/oidcVcNonce", "",
        {
            "Content-Type": "application/json"
        }, (res) => {
            assert(res.data.c_nonce !== undefined);
            assert(res.data.c_nonce_expires_in !== undefined);
            return res.data.c_nonce;
        }, (error) => {
            throw `Operation failed: ${error}`;
        });
    await cas.log(`Fetched nonce ${cNonce}. Now fetching credential offer`);

    const body = JSON.stringify({
        "principal": "casuser",
        "credentialConfigurationIds": ["myorg"]
    });
    const payload = JSON.parse(
        await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcCredentialOfferTransactions?scope=openid", "POST", {
            "Authorization": `Basic ${btoa("client:secret")}`,
            "Content-Length": body.length,
            "Content-Type": "application/json"
        }, 200, body)
    );
    assert(payload.transactionId !== undefined);
    assert(payload.credentialOfferUri !== undefined);
    assert(payload.txCode !== undefined);
    assert(payload.txCode !== payload.transactionId,
        "The transaction code must never be the transaction identifier");

    await cas.log(`Fetched credential offer URI ${payload.credentialOfferUri}`);
    await cas.log("Now fetching credential offer...");

    const offer = await cas.doGet(payload.credentialOfferUri,
        (res) => {
            assert(res.status === 200);
            assert(res.data.credential_issuer === "https://localhost:8443/cas/oidc");
            assert(res.data.credential_configuration_ids[0] === "myorg");
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"] !== undefined);
            const txCode = res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["tx_code"];
            assert(txCode !== undefined);
            assert(txCode.value === undefined, "The credential offer must never disclose the transaction code");
            assert(txCode.length === payload.txCode.length);
            assert(txCode.input_mode !== undefined);
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["issuer_state"] !== undefined);
            return {
                preAuthorizedCode: res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"]
            };
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    await cas.log("Now fetching access token for pre-authorized code...");
    
    const baseParams = "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code&&scope=openid"
        + `&pre-authorized_code=${offer.preAuthorizedCode}`;

    await cas.log("Verifying the transaction identifier is rejected as a transaction code...");
    const rejected = await cas.doRequest(
        `https://localhost:8443/cas/oidc/token?${baseParams}&tx_code=${payload.transactionId}`, "POST", {
            "Content-Type": "application/json",
            "Authorization": `Basic ${btoa("client:secret")}`
        }, 0, "");
    assert(!rejected.includes("access_token"),
        "The transaction identifier must not be accepted as the transaction code");

    await cas.log("Verifying a missing transaction code is rejected...");
    const missing = await cas.doRequest(`https://localhost:8443/cas/oidc/token?${baseParams}`, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, 0, "");
    assert(!missing.includes("access_token"), "A missing transaction code must not be accepted");

    let url = `https://localhost:8443/cas/oidc/token?${baseParams}&tx_code=${payload.txCode}`;
    await cas.log(`Calling ${url}`);

    const accessToken = await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.c_nonce !== undefined);
        assert(res.data.c_nonce_expires_in !== undefined);
        return res.data.access_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
    await cas.log(`Fetched access token for pre-authorized code ${accessToken}`);
    
    url = "https://localhost:8443/cas/oidc/oidcVcCredential";
    await cas.log(`Calling ${url}`);

    const proof = await createPublicKey();
    const credentialRequest = JSON.stringify({
        credential_configuration_id: "myorg",
        proof: {
            proof_type: "jwt",
            jwt: proof
        }
    });
    const result = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
    }, 200, credentialRequest));
    await cas.log(result);
    assert(result.credential !== undefined);
    assert(result.format === "dc+sd-jwt");

    const parts = result.credential.split("~");
    const issuerJwt = parts[0];
    const decoded = await cas.decodeJwt(issuerJwt);
    assert(decoded !== undefined && decoded !== null);
    assert(decoded.sub === "casuser");
    assert(decoded.email === "casuser@example.org");
    assert(decoded.given_name === "CAS");
    assert(decoded.family_name === "User");
    assert(decoded.score === 95.5);
    assert(decoded.roles.length === 2);
    assert(decoded.roles.includes("user"));
    assert(decoded.roles.includes("admin"));
    assert(decoded.student_id === undefined);
})();
