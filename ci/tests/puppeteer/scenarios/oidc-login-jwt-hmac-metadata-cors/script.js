const assert = require("assert");
const cas = require("../../cas.js");
const crypto = require("crypto");

async function verifyAssertionRefused(clientId, assertion, reason) {
    await cas.log(`Client authentication must fail: ${reason}`);
    let params = "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&";
    params += `client_assertion=${assertion}&client_id=${clientId}&grant_type=client_credentials&scope=openid`;
    await cas.doPost(`https://localhost:8443/cas/oidc/token?${params}`, "", {
        "Content-Type": "application/json"
    }, (res) => {
        throw `Client assertion should have been refused (${reason}) but produced: ${JSON.stringify(res.data)}`;
    }, (error) => {
        assert(error.response !== undefined, `No response: ${error}`);
        cas.log(`Status: ${error.response.status}`);
        assert(error.response.status === 401);
    });
}

async function createClientAssertion(clientId, secret, alg) {
    const now = Math.floor(Date.now() / 1000);
    return cas.createJwt({
        "jti": crypto.randomUUID(),
        "iss": clientId,
        "sub": clientId,
        "client_id": clientId,
        "aud": "https://localhost:8443/cas/oidc/token",
        "iat": now,
        "nbf": now - 60,
        "exp": now + 300
    }, secret, alg);
}

(async () => {
    const clientSecret = "enTHR15K28p0N6f404HaC9Vp1cfIBgQiHhmbgBiO7UHEnSiNJudxtDhPQNFjFQtOVSjEYu0pr5yxEeBAiO6IlA";
    const jwt = await cas.createJwt({
        "jti": crypto.randomUUID(),
        "iss": "client",
        "aud": "https://localhost:8443/cas/oidc/token",
        "exp": 185542587100,
        "iat": 1653737633,
        "nbf": 1653737573,
        "sub": "client",
        "client_id": "client"
    }, clientSecret, "HS512");

    let params = "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&";
    params += `client_assertion=${jwt}&client_id=client&grant_type=client_credentials&scope=openid`;

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.id_token !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const wrongSecret = crypto.randomBytes(64).toString("base64url");
    await verifyAssertionRefused("client", await createClientAssertion("client", wrongSecret, "HS512"),
        "assertion is signed with a secret other than the client secret");
    await verifyAssertionRefused("shortsecretclient", await createClientAssertion("shortsecretclient", "0123456789abcdef", "HS256"),
        "a 16-byte client secret is shorter than the 32 bytes HS256 requires");

    const discoveryUrl = "https://localhost:8443/cas/oidc/.well-known";
    await cas.log(`Calling discovery URL ${discoveryUrl}`);
    await cas.doRequest(discoveryUrl, "OPTIONS",  {
        "Content-Type": "application/json",
        "Origin": "https://myapp:4200",
        "Host": "localhost:8443",
        "Access-Control-Request-Method": "GET",
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }, 200);
})();
