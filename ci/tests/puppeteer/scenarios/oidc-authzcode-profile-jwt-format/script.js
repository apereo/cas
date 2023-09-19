const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://github.com/apereo/cas";

    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    await cas.click(page, "#allow");
    await page.waitForNavigation();

    let code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, async res => {
        await cas.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        assert(decoded.sub !== null);
        assert(decoded["preferred_username"] == null)
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null, "Access Token cannot be null");

    let profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${accessToken}`;
    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        'Accept': "application/jwt"
    }, res => {
        cas.log(res.data);
        assert(res.data != null)
    }, error => {
        throw `Operation failed: ${error}`;
    });

    await browser.close();
})();
