const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const jwkToPem = require("jwk-to-pem");

const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});

async function getAccessToken() {
    const params = `grant_type=client_credentials&scope=${encodeURIComponent("openid client_jwks_registration_scope")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;

    return cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        return res.data;
    }, (error) => {
        throw `Operation failed: ${error}`;
    }, false);
}

async function introspect(token) {
    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => {
            assert(res.data.active === true);
            assert(res.data.aud === "client");
            assert(res.data.sub === "client");
            assert(res.data.iss === "https://localhost:8443/cas/oidc");
            assert(res.data.client_id === "client");
            assert(res.data.token === token);
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
}

async function createPublicKey() {
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
        "aud": "https://localhost:8443/cas/oidc/token"
    }, privateKey, "RS256", {
        header: {
            jwk: publicJwk
        }
    });
}

async function registerPublicKey(accessToken, proof) {
    const url = "https://localhost:8443/cas/oidc/jwks/clients";
    const body = JSON.stringify({"proof": proof});
    await cas.logb(`Sending public key registration request with body: ${body}`);
    return JSON.parse(await cas.doRequest(url, "POST",
        {
            "Accept": "application/json",
            "Content-Length": body.length,
            "Content-Type": "application/json",
            "Authorization": `Bearer ${accessToken}`
        }, 200, body));
}

async function createJwtAssertion(jkt) {
    const assertion = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "client",
        "aud": "https://localhost:8443/cas/oidc/token",
        "exp": 185542587100,
        "iat": 1653737633,
        "sub": "casuser",
        "client_id": "client"
    }, privateKey, "RS256", {
        header: {
            "kid": jkt
        }
    });
    await cas.logb(`Created assertion JWT: ${assertion}`);
    return assertion;
}

(async () => {
    const payload = await getAccessToken();
    await introspect(payload.access_token);
    const proof = await createPublicKey();
    await cas.logb(`Created proof JWT: ${proof}`);
    const result = await registerPublicKey(payload.access_token, proof);
    await cas.logb(`Registered public key with thumbprint: ${JSON.stringify(result)}`);

    const assertion = await createJwtAssertion(result.jkt);

    let accessTokenParams = `assertion=${assertion}&`;
    accessTokenParams += `scope=${encodeURIComponent("openid email profile")}&`;
    accessTokenParams += "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer";

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}`;
    await cas.doPost(accessTokenUrl, "", {"Content-Type": "application/json"},
        async (res) => {
            const accessToken = res.data.access_token;
            const refreshToken = res.data.refresh_token;
            const idToken = res.data.id_token;
            assert(accessToken !== undefined, "Access Token cannot be null");
            assert(refreshToken !== undefined, "Refresh Token cannot be null");
            assert(idToken !== undefined, "ID Token cannot be null");

            await cas.log("Decoding JWT ID token...");
            const decoded = await cas.decodeJwt(idToken);
            assert(decoded.sub !== undefined);
            assert(decoded.aud !== undefined);
            assert(decoded.jti !== undefined);
            assert(decoded.iss !== undefined);
            assert(decoded.exp !== undefined);
            assert(decoded.client_id !== undefined);
            assert(decoded.preferred_username !== undefined);
        },
        (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

})();
