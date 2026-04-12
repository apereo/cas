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
            assert(res.data.c_nonce_expires_at !== undefined);
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

    await cas.log(`Fetched credential offer URI ${payload.credentialOfferUri}`);
    await cas.log("Now fetching credential offer...");

    const offer = await cas.doGet(payload.credentialOfferUri,
        (res) => {
            assert(res.status === 200);
            assert(res.data.credential_issuer === "https://localhost:8443/cas/oidc");
            assert(res.data.credential_configuration_ids[0] === "myorg");
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"] !== undefined);
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["tx_code_required"] === true);
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["tx_code"] !== undefined);
            assert(res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["issuer_state"] !== undefined);
            return {
                txCode: res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["tx_code"],
                preAuthorizedCode: res.data.grants["urn:ietf:params:oauth:grant-type:pre-authorized_code"]["pre-authorized_code"]
            };
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    await cas.log("Now fetching access token for pre-authorized code...");
    
    let params = "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code&&scope=openid";
    params += `&pre-authorized_code=${offer.preAuthorizedCode}&tx_code=${offer.txCode}`;
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    const accessToken = await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.c_nonce !== undefined);
        assert(res.data.c_nonce_expires_at !== undefined);
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
    assert(result.format === CredentialConfigurationFormats.VC);

    const decoded = await cas.decodeJwt(result.credential);
    assert(decoded.sub === "client");
    assert(decoded.email === "casuser@example.org");
    assert(decoded.given_name === "CAS");
    assert(decoded.family_name === "User");
    assert(decoded.score === 95.5);
    assert(decoded.roles.length === 2);
    assert(decoded.roles.includes("user"));
    assert(decoded.roles.includes("admin"));
    assert(decoded.student_id === "S12345");
})();
