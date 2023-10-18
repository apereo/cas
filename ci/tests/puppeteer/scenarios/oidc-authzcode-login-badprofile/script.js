const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + "&client_id=client&scope=badprofile&"
        + "redirect_uri=https://apereo.github.io&nonce=3d3a7457f9ad3&"
        + "state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential"
        + "%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D";

    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);

    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, '#userInfoClaims');
    await cas.assertVisibility(page, '#scopes');
    await cas.assertVisibility(page, '#openid');
    await cas.assertVisibility(page, '#informationUrl');
    await cas.assertVisibility(page, '#privacyUrl');
    await cas.assertVisibility(page, '#name');
    await cas.assertVisibility(page, '#phone_number');

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=client&client_secret=secret&redirect_uri=https://apereo.github.io&code=${code}`;
    let payload = await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        return res.data;
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token != null);
    assert(payload.token_type != null);
    assert(payload.expires_in != null);
    assert(payload.scope != null);

    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== null);
    assert(decoded.client_id !== null);
    assert(decoded["preferred_username"] !== null);

    assert(decoded["identity-name"] === undefined);
    assert(decoded["common-name"] === undefined);
    assert(decoded["lastname"] === undefined);
    assert(decoded["cn"] === undefined);
    assert(decoded["family_name"] === undefined);
    assert(decoded["name"] === undefined);

    let profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token }`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        assert(decoded["common-name"] === undefined);
        assert(decoded["lastname"] === undefined);

        assert(res.data["cn"] === undefined);
        assert(res.data["name"] === undefined);
        assert(res.data["family_name"] === undefined);
        assert(res.data.sub != null);
        assert(Object.keys(res.data).length === 1);
    }, error => {
        throw `Operation failed: ${error}`;
    });

    await browser.close();
})();
