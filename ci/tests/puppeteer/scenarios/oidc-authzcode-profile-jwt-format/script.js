
const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");
const jose = require("jose");

const ISSUER = "https://localhost:8443/cas/oidc";

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://github.com/apereo/cas";

    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        const decoded = await cas.decodeJwt(res.data.id_token);

        assert(decoded.sub !== undefined);
        assert(decoded["preferred_username"] === undefined);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined, "Access Token cannot be null");

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${accessToken}`;
    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        "Accept": "application/jwt"
    }, (res) => {
        cas.log(res.data);
        assert(res.data !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await verifyEncryptedResponses(page);
    await verifyOptionalIdTokenEncryption(page);
    await cas.closeBrowser(browser);
})();

/**
 * Runs the authorization code flow for a client and returns the token response.
 *
 * @param page the page, with a single sign-on session
 * @param clientId the client id
 * @param scope the scopes
 * @returns the token response
 */
async function tokensFor(page, clientId, scope) {
    const redirectUri = `https://localhost:9859/anything/${clientId}`;
    await cas.goto(page, `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}`
        + `&scope=${encodeURIComponent(scope)}&redirect_uri=${redirectUri}&nonce=n${Date.now()}&state=s${Date.now()}`);
    await cas.sleep(1000);
    const code = await cas.assertParameter(page, "code");
    return cas.doPost(`https://localhost:8443/cas/oidc/token?grant_type=authorization_code&client_id=${clientId}`
        + `&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`, "", {"Content-Type": "application/json"},
    (res) => res.data, (error) => {
        throw `Operation failed to obtain tokens: ${error}`;
    });
}

/**
 * Decrypts a response encrypted for the client and verifies the signed token inside it with the keys of CAS.
 *
 * @param jwe the encrypted response
 * @returns the header and claims of the signed token inside
 */
async function decryptAndVerify(jwe) {
    const privateKey = await jose.importJWK(JSON.parse(fs.readFileSync(path.join(__dirname, "client-private.jwk"), "utf8")), "RSA-OAEP-256");
    const {plaintext, protectedHeader} = await jose.compactDecrypt(jwe, privateKey);
    assert.equal(protectedHeader.alg, "RSA-OAEP-256");
    assert.equal(protectedHeader.enc, "A256GCM");
    const keys = jose.createRemoteJWKSet(new URL(`${ISSUER}/jwks`));
    const {payload, protectedHeader: signedHeader} = await jose.jwtVerify(new TextDecoder().decode(plaintext), keys);
    assert.equal(signedHeader.alg, "RS256");
    return payload;
}

/**
 * A client with its own encryption key gets user info and token introspection responses signed by CAS and encrypted
 * for it, JWT access tokens signed with the algorithm it asks for, and JWT refresh tokens that it can redeem.
 *
 * @param page the page, with a single sign-on session
 */
async function verifyEncryptedResponses(page) {
    const tokens = await tokensFor(page, "encrypted", "openid profile email");

    await cas.logg("The JWT access token is signed with the algorithm of the client");
    const accessTokenHeader = (await cas.decodeJwt(tokens.access_token, true)).header;
    assert.equal(accessTokenHeader.alg, "PS256");

    await cas.logg("User info is signed and encrypted for the client");
    const userInfo = await cas.doPost(`https://localhost:8443/cas/oidc/profile?access_token=${tokens.access_token}`, "",
        {"Accept": "application/jwt"}, (res) => res.data, (error) => {
            throw `Operation failed: ${error}`;
        });
    assert.equal(userInfo.split(".").length, 5, "User info must be a JWE");
    const profile = await decryptAndVerify(userInfo);
    assert.equal(profile.sub, "casuser");
    assert.equal(profile.email, "casuser@apereo.org");

    await cas.logg("Token introspection as JWT is signed and encrypted for the client");
    const introspection = await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${tokens.access_token}`,
        (res) => res.data, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": `Basic ${btoa("encrypted:secret")}`,
            "Accept": "application/token-introspection+jwt"
        });
    assert.equal(introspection.split(".").length, 5, "Introspection response must be a JWE");
    const introspected = await decryptAndVerify(introspection);
    assert.equal(introspected.token_introspection.active, true);
    assert.equal(introspected.token_introspection.client_id, "encrypted");

    await cas.logg("The refresh token is a JWT and can be redeemed");
    assert.equal(tokens.refresh_token.split(".").length, 3, "Refresh token must be a JWT");
    const refreshed = await cas.doPost("https://localhost:8443/cas/oidc/token?grant_type=refresh_token&client_id=encrypted"
        + `&client_secret=secret&refresh_token=${tokens.refresh_token}`, "", {"Content-Type": "application/json"},
    (res) => res.data, (error) => {
        throw `Operation failed to redeem the refresh token: ${error}`;
    });
    assert(refreshed.access_token !== undefined);
}

/**
 * A client that asks for encrypted ID tokens but has no encryption key, and allows encryption to be skipped, gets a
 * signed ID token instead of a failure.
 *
 * @param page the page, with a single sign-on session
 */
async function verifyOptionalIdTokenEncryption(page) {
    const tokens = await tokensFor(page, "optional", "openid profile");
    assert.equal(tokens.id_token.split(".").length, 3, "ID token must be signed only");
    const idToken = await cas.decodeJwt(tokens.id_token);
    assert.equal(idToken.aud, "optional");
    assert.equal(idToken.iss, ISSUER);
}
