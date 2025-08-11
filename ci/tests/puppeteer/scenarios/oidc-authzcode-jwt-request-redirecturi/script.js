
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const request = "eyJhbGciOiJub25lIn0.eyJyZXNwb25zZV90eXBlIjoiY29kZSIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vbG9jYWxob3N0Ojk4NTkvYW55dGhpbmcvY2FzIiwic3RhdGUiOiJ2SU4xYjBZNENrIiwibm9uY2UiOiIxTjltcVBPOWZ0IiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOiJvcGVuaWQifQ.";
    const url = `https://localhost:8443/cas/oidc/authorize?request=${request}&scope=openid&redirect_uri=https://unknown.net`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

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
        assert(decoded.aud !== undefined);
        assert(decoded.jti !== undefined);
        assert(decoded.sid !== undefined);
        assert(decoded.iss !== undefined);
        assert(decoded.state !== undefined);
        assert(decoded.nonce !== undefined);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    await cas.closeBrowser(browser);
})();
