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

    const params = "grant_type=client_credentials&scope=openid";
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    const accessToken = await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        return res.data.access_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    url = "https://localhost:8443/cas/oidc/oidcVcCredential";
    await cas.log(`Calling ${url}`);

    const proof = await createPublicKey();
    
    const body = JSON.stringify({
        credential_configuration_id: "myorg",
        proof: {
            proof_type: "jwt",
            jwt: proof
        }
    });
    const result = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
    }, 200, body));
    await cas.log(result);
    assert(result.credential !== undefined);
    assert(result.format === "vc+sd-jwt");

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
