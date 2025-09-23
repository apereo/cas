
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const redirectUrl = "https://localhost:9859/anything/cas";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("openid badprofile")}&`
        + `redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&`
        + "state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential"
        + "%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D";

    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);

    await cas.sleep(1000);
    await cas.assertVisibility(page, "#userInfoClaims");
    await cas.assertVisibility(page, "#scopes");
    await cas.assertVisibility(page, "#openid");
    await cas.assertVisibility(page, "#informationUrl");
    await cas.assertVisibility(page, "#privacyUrl");
    await cas.assertVisibility(page, "#name");
    await cas.assertVisibility(page, "#phone_number");

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.id_token !== undefined);
    assert(payload.scope === "openid");

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== undefined);
    assert(decoded.client_id !== undefined);
    assert(decoded["preferred_username"] !== undefined);
    assert(decoded["name"] !== undefined);

    assert(decoded["identity-name"] === undefined);
    assert(decoded["common-name"] === undefined);
    assert(decoded["lastname"] === undefined);
    assert(decoded["cn"] === undefined);
    assert(decoded["family_name"] === undefined);

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token }`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data["name"] !== undefined);
        assert(decoded["common-name"] === undefined);
        assert(decoded["lastname"] === undefined);
        assert(res.data["cn"] === undefined);
        assert(res.data["family_name"] === undefined);
        assert(res.data.sub !== undefined);
        assert(Object.keys(res.data).length === 2);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.closeBrowser(browser);
})();
