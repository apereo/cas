const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

(async () => {
    const configFilePath = path.join(__dirname, "/keystore.json");
    const key = JSON.parse(fs.readFileSync(configFilePath)).keys[0];
    await cas.log(`Using JWK:\n${JSON.stringify(key)}\n`);
    
    const privateKey = jwkToPem(key, {private: true});
    await cas.log(`Using private key (PEM):\n${privateKey}`);
    
    const jwt = await cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "https://localhost:8443/cas/oidc",
        "aud": "client",
        "exp": 185542587100,
        "iat": 1653737633,
        "nbf": 1653737573,
        "sub": "casuser",
        "client_id": "client"
    }, privateKey);

    let params = "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&";
    params += `client_assertion=${jwt}&client_id=client&`;
    params += "grant_type=client_credentials&";
    params += "scope=openid";

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

})();
