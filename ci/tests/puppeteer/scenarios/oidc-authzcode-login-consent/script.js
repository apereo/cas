const cas = require("../../cas.js");
const assert = require("assert");

async function verifyAccessTokenAndProfile(context, client, scopes, consent, defined, login) {
    const page = await cas.newPage(context);
    const redirectUri = `http://localhost:9889/anything/app${client}`;
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client${client}&scope=${encodeURIComponent(scopes.join(" "))}`
        + `&redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.goto(page, url);
    if (login) {
        await cas.sleep(1000);
        await cas.loginWith(page);
    }
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#scopes");
    await cas.assertVisibility(page, "#informationUrl");
    await cas.assertVisibility(page, "#privacyUrl");
    for (const id of consent) {
        await cas.assertVisibility(page, id);
    }

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client${client}&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== undefined);
    assert(decoded.client_id !== undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        for (const attr of defined) {
            assert(res.data[attr] !== undefined);
        }
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
    return {redirectUri, url, code, accessTokenUrl, payload};
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    const context = await browser.createBrowserContext();
    await verifyAccessTokenAndProfile(context, 1, ["openid"], ["#openid"], ["sub"], true);
    await verifyAccessTokenAndProfile(context, 1, ["openid", "profile", "MyCustomScope"], ["#profile", "#MyCustomScope"], ["sub", "name", "family_name", "cn"], false);
    await verifyAccessTokenAndProfile(context, 2, ["openid", "profile", "MyCustomScope"], ["#openid", "#profile", "#MyCustomScope"], ["sub", "name", "family_name", "cn"], false);
    await context.close();

    await cas.closeBrowser(browser);
})();
