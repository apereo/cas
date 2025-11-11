const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const jwkToPem = require("jwk-to-pem");
(async () => {

    const configFilePath = path.join(__dirname, "/keystore.json");
    const key = JSON.parse(fs.readFileSync(configFilePath)).keys[0];
    await cas.log(`Using JWK:\n${JSON.stringify(key)}\n`);

    const privateKey = jwkToPem(key, {private: true});
    await cas.log(`Using private key (PEM):\n${privateKey}`);

    const assertion = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "client",
        "aud": "https://localhost:8443/cas/oidc/token",
        "exp": 185542587100,
        "iat": 1653737633,
        "sub": "casuser",
        "client_id": "client"
    }, privateKey);
    await cas.logb(`Created assertion JWT:${assertion}`);

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
