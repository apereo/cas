const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

async function executeFlow(browser, redirectUri, clientId, accessTokenSecret) {
    const page = await cas.newPage(browser);

    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${encodeURIComponent(redirectUri)}&client_id=${clientId}&scope=profile&state=9qa3`;

    await cas.goto(page, url);
    await cas.log(`Page URL: ${page.url()}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);

    let code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = `client_id=${clientId}&`;
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${encodeURIComponent(redirectUri)}`;

    let accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        cas.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null);

    if (clientId === "client") {
        await cas.verifyJwt(accessToken, accessTokenSecret, {
            algorithms: ["HS512"],
            complete: true
        });
    } else if (clientId === "client2") {
        await cas.decryptJwtWithSecret(accessToken, accessTokenSecret, {
            contentEncryptionAlgorithms: ["A128CBC-HS256", "A128CBC-HS512"],
            keyManagementAlgorithms: ["dir"]
        });
    }
    
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

    await cas.goto(page, "https://localhost:8443/cas/logout");
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    await executeFlow(browser, "https://apereo.github.io","client", process.env.OAUTH_ACCESS_TOKEN_SIGNING_KEY);
    await executeFlow(browser, "https://apereo.github.io","client2",process.env.OAUTH_ACCESS_TOKEN_ENCRYPTION_KEY);
    await browser.close();
})();
