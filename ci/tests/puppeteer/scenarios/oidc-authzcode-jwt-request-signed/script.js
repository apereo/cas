
const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const request = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMzQ1Njc4OTAiLCJvcmcuYXBlcmVvLmNhcy5zZXJ2aWNlcy5SZWdpc3RlcmVkU2VydmljZSI6IjIxMzMyNDg2MjEifQ.CnsKICAic3ViIjogImNhc3VzZXIiLAogICJzY29wZSI6ICJvcGVuaWQiLAogICJpc3MiOiAiaHR0cHM6Ly9jYXMuZXhhbXBsZS5vcmciLAogICJyZXNwb25zZV90eXBlIjogImNvZGUiLAogICJyZWRpcmVjdF91cmkiOiAiaHR0cHM6Ly9sb2NhbGhvc3Q6OTg1OS9hbnl0aGluZy9jYXMiLAogICJpYXQiOiAxNjQ2NzMxODE5LAogICJqdGkiOiAiZWVkY2Q5Y2ItNDA1MS00ODAyLWFmYWUtYmFkMzU1NDNiYjU3IiwKICAiY2xpZW50X2lkIjogImNsaWVudCIKfQo.x1LLYmH_8jAbdfiYlQ5hreI2IY3m2olGbBM4Hi2O5wUE_fRPhFD3Z_YsMbK4Qp9Us1aqn3VdYaEovnBzr0W37WJhzTysWId2cffajH_z9xF47DIlkc5PXWNeWo0g8N_lMvFWkcQQNw1inU8J9370-sGZXyJSggLmCcbNjWrWGvFKtSBLMY-v-_HSnFKCDq9LK6mLN9HFqzWG3GhDrcCtl8JIsd1JC44QSoJ4WsxWIoUtto_j6EFwNyMC_2xI6E68L9uAbFfknXz3siuzp9NN9_M_peRGKXigKrZg8et9ggsRHqSHj2j5T-P9FmaFDlJz-rmCHytKE0KgKiYBkWG9Cg";
    const url = `https://localhost:8443/cas/oidc/authorize?request=${request}&client_id=client&redirect_uri=https://unknown.net`;

    await cas.log(`Browsing to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const redirectUrl = "https://localhost:9859/anything/cas";
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
        assert(decoded.sub === "casuser");
        assert(!decoded.jti.startsWith("TGT-"));
        assert(decoded.aud === "client");
        assert(decoded.jti !== undefined);
        assert(decoded.iat !== undefined);
        assert(decoded.sid !== undefined);
        assert(decoded.iss === "https://localhost:8443/cas/oidc");
        assert(decoded.client_id === "client");
        assert(decoded.auth_time !== undefined);
        assert(decoded.preferred_username === "casuser");
        assert(decoded.amr[0] === "Static Credentials");
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    
    await verifyEncryptedRequestObject(page, request);
    await verifyTamperedRequestObject(page, request);
    await cas.closeBrowser(browser);
})();

/**
 * The same signed request object, encrypted to the encryption key CAS publishes in its JWKS (OpenID Connect Core 1.0,
 * section 6.3.1): CAS decrypts it and verifies the nested JWT with the client's keys.
 */
async function verifyEncryptedRequestObject(page, signedRequest) {
    const jwks = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/jwks", "GET", {}, 200));
    const encryptionKey = jwks.keys.find((key) => key.use === "enc" && key.kty === "RSA");
    assert(encryptionKey !== undefined, "CAS publishes no RSA encryption key");
    const encryptedRequest = await new jose.CompactEncrypt(new TextEncoder().encode(signedRequest))
        .setProtectedHeader({alg: "RSA-OAEP-256", enc: "A256GCM", cty: "JWT", kid: encryptionKey.kid})
        .encrypt(await jose.importJWK(encryptionKey, "RSA-OAEP-256"));
    assert.equal(encryptedRequest.split(".").length, 5);

    const url = `https://localhost:8443/cas/oidc/authorize?request=${encryptedRequest}&client_id=client&redirect_uri=https://unknown.net`;
    await cas.log(`Browsing to ${url}`);
    await cas.goto(page, url);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code} for the encrypted request object`);
    assert(page.url().startsWith("https://localhost:9859/anything/cas"), page.url());
}

/**
 * The same request object with a broken signature is refused with invalid_request_object at the registered redirect URI
 * (OpenID Connect Core 1.0, section 6.3.2), instead of being ignored in favor of the query parameters.
 */
async function verifyTamperedRequestObject(page, signedRequest) {
    const tampered = `${signedRequest.substring(0, signedRequest.lastIndexOf(".") + 1)}AAAA`;
    const redirectUri = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oidc/authorize?request=${tampered}&client_id=client&redirect_uri=${redirectUri}`;
    await cas.log(`Browsing to ${url}`);
    await cas.goto(page, url);
    await cas.sleep(1000);
    assert(page.url().startsWith(redirectUri), page.url());
    const error = new URL(page.url()).searchParams.get("error");
    assert.equal(error, "invalid_request_object");
}
