const assert = require("assert");
const cas = require("../../cas.js");
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

const tokenEndpoint = "https://localhost:8443/cas/oidc/token";
const assertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

async function createClientAssertion(clientId, signingKey, claims = {}) {
    const now = Math.floor(Date.now() / 1000);
    return cas.createJwt({
        "jti": crypto.randomUUID(),
        "iss": clientId,
        "sub": clientId,
        "client_id": clientId,
        "aud": tokenEndpoint,
        "iat": now,
        "nbf": now - 60,
        "exp": now + 300,
        ...claims
    }, signingKey);
}

async function requestClientCredentials(clientId, assertion, successHandler, failureHandler) {
    let params = `client_assertion_type=${assertionType}&`;
    params += `client_assertion=${assertion}&client_id=${clientId}&`;
    params += "grant_type=client_credentials&scope=openid";
    await cas.doPost(`${tokenEndpoint}?${params}`, "", {
        "Content-Type": "application/json"
    }, successHandler, failureHandler);
}

async function verifyAssertionAccepted(clientId, assertion) {
    await requestClientCredentials(clientId, assertion, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyAssertionRefused(clientId, assertion, reason) {
    await cas.log(`Client authentication must fail: ${reason}`);
    await requestClientCredentials(clientId, assertion, (res) => {
        throw `Client assertion should have been refused (${reason}) but produced: ${JSON.stringify(res.data)}`;
    }, (error) => {
        assert(error.response !== undefined, `No response: ${error}`);
        cas.log(`Status: ${error.response.status}`);
        assert(error.response.status === 401);
    });
}

async function verifyBasicAuthentication(clientId, expectedStatus) {
    const authorization = `Basic ${Buffer.from(`${clientId}:secret`).toString("base64")}`;
    await cas.doPost(`${tokenEndpoint}?grant_type=client_credentials&scope=openid`, "", {
        "Content-Type": "application/json",
        "Authorization": authorization
    }, (res) => {
        assert(expectedStatus === 200, `Basic authentication for ${clientId} should be refused but produced: ${JSON.stringify(res.data)}`);
        assert(res.data.access_token !== undefined);
    }, (error) => {
        assert(error.response !== undefined, `No response: ${error}`);
        cas.log(`Status: ${error.response.status}`);
        assert(error.response.status === expectedStatus);
    });
}

async function verifyAuthorizationCodeWithPrivateKeyJwt(privateKey) {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUri = "https://localhost:9859/anything/codeclient";
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=codeclient"
        + `&scope=openid%20profile&redirect_uri=${redirectUri}&state=Ah6Rb2Q9&nonce=Fq81mZ3u`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.assertPageUrlStartsWith(page, redirectUri);
    const code = await cas.assertParameter(page, "code");

    const assertion = await createClientAssertion("codeclient", privateKey);
    let params = `client_assertion_type=${assertionType}&client_assertion=${assertion}&client_id=codeclient&`;
    params += `grant_type=authorization_code&redirect_uri=${redirectUri}&code=${code}`;
    await cas.doPost(`${tokenEndpoint}?${params}`, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.id_token !== undefined);
        const idToken = await cas.decodeJwt(res.data.id_token);
        assert(idToken.aud === "codeclient");
        assert(idToken.sub === "casuser");
    }, (error) => {
        throw `Authorization code grant with private_key_jwt failed: ${error}`;
    });
    await cas.closeBrowser(browser);
}

(async () => {
    const configFilePath = path.join(__dirname, "/keystore.json");
    const key = JSON.parse(fs.readFileSync(configFilePath)).keys[0];
    await cas.log(`Using JWK:\n${JSON.stringify(key)}\n`);

    const privateKey = jwkToPem(key, {private: true});
    await cas.log(`Using private key (PEM):\n${privateKey}`);

    const jwt = await cas.createJwt({
        "jti": crypto.randomUUID(),
        "iss": "client",
        "aud": "https://localhost:8443/cas/oidc/token",
        "exp": 185542587100,
        "iat": 1653737633,
        "nbf": 1653737573,
        "sub": "client",
        "client_id": "client"
    }, privateKey);
    await verifyAssertionAccepted("client", jwt);
    await verifyAssertionRefused("client", jwt, "the assertion identifier (jti) was already used");

    const foreignKey = crypto.generateKeyPairSync("rsa", {modulusLength: 2048})
        .privateKey.export({type: "pkcs8", format: "pem"});
    await verifyAssertionRefused("client", await createClientAssertion("client", foreignKey),
        "signed by a key that is not in the client's key set");
    await verifyAssertionRefused("client", await createClientAssertion("client", privateKey,
        {"aud": "https://localhost:8443/cas/oidc/authorize"}), "audience is not the token endpoint");
    const now = Math.floor(Date.now() / 1000);
    await verifyAssertionRefused("client", await createClientAssertion("client", privateKey,
        {"iat": now - 7200, "nbf": now - 7200, "exp": now - 3600}), "assertion has expired");
    await verifyAssertionRefused("client", await createClientAssertion("client", privateKey,
        {"sub": "basicclient"}), "subject is not the client");

    await cas.log("The registered token endpoint authentication method is enforced");
    await verifyBasicAuthentication("client", 401);
    await verifyBasicAuthentication("basicclient", 200);
    await verifyAssertionRefused("basicclient", await createClientAssertion("basicclient", privateKey),
        "client registered for client_secret_basic presents a client assertion");

    await cas.log("private_key_jwt authenticates the authorization code grant");
    await verifyAuthorizationCodeWithPrivateKeyJwt(privateKey);
})();
