const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

async function executeFlow(browser, redirectUri, clientId, accessTokenSecret) {
    const page = await cas.newPage(browser);

    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${encodeURIComponent(redirectUri)}&client_id=${clientId}&scope=profile&state=9qa3`;

    await cas.goto(page, url);
    console.log(`Page URL: ${page.url()}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);

    let code = await cas.assertParameter(page, "code");
    await console.log(`OAuth code ${code}`);

    let accessTokenParams = `client_id=${clientId}&`;
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${encodeURIComponent(redirectUri)}`;

    let accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
    await console.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        console.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null);

    const params = new URLSearchParams();
    params.append('access_token', accessToken);

    await cas.doPost('https://localhost:8443/cas/oauth2.0/profile', params, {},
        res => {
            let result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === clientId);
            assert(result.service === "https://apereo.github.io");
        }, error => {
            throw error;
        });

    const parts = accessToken.split('.');
    const badAccessToken = parts[0] + '.Z' + parts[1] + '.' + parts[2];
    const badParams = new URLSearchParams();
    badParams.append('access_token', badAccessToken);

    await cas.doPost('https://localhost:8443/cas/oauth2.0/profile', badParams, {},
        res => {
            console.log(res.data);
            throw 'Operation must fail to get the profile with a bad access token';
        }, error => {
            assert(error.response.status === 401);
            assert(error.response.data.error === 'invalid_request');
        });

    await cas.goto(page, "https://localhost:8443/cas/logout");
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    await executeFlow(browser, "https://apereo.github.io","client", process.env.OAUTH_ACCESS_TOKEN_SIGNING_KEY);
    await executeFlow(browser, "https://apereo.github.io","client2",process.env.OAUTH_ACCESS_TOKEN_ENCRYPTION_KEY);
    await browser.close();
})();
