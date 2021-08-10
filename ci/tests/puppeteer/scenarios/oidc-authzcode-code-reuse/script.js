const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://github.com/apereo/cas";

    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.click(page, "#allow");
    await page.waitForNavigation();

    let code = await cas.assertParameter(page, "code");
    console.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    console.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, async function (res) {
        console.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
        console.log(`Received access token ${accessToken}`);

        console.log("Decoding ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        assert(decoded.sub !== null)
        assert(decoded["preferred_username"] == null)
    }, function (error) {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null, "Access Token cannot be null")

    let profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${accessToken}`;
    console.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json"
    }, function (res) {
        console.log(res.data);
        assert(res.data.email != null)
        assert(res.data.gender != null)
        assert(res.data.name != null)
        assert(res.data["preferred_username"] != null)
    }, function (error) {
        throw `Operation failed: ${error}`;
    });

    console.log(`Trying to re-use OAuth code ${accessTokenUrl}`);
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, function () {
        throw `OAuth code ${code} cannot be used again`;
    }, function (error) {
        console.log(error.response.data)
        assert(error.response.data.error === 'invalid_grant')
    });

    console.log(`Reusing OAuth code ${code} should have revoked access token ${accessToken}`);
    console.log(`Calling user profile again with revoked access token: ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json"
    }, function () {
        throw `Access token ${accessToken} should have been removed and rejected with code reused`;
    }, function (error) {
        assert(error.response.status === 401)
        console.log(error.response.data);
        assert(error.response.data.error === "expired_accessToken");
    });

    await browser.close();
})();
