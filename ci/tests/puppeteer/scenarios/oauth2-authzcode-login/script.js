const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');


(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUri = "https%3A%2F%2Fapereo.github.io";
    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=profile&state=9qa3`;
    
    await page.goto(url);
    console.log(`Page URL: ${page.url()}`)
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    let code = await cas.assertParameter(page, "code");
    console.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

    let accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
    console.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, function (res) {
        console.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
    }, function (error) {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null)

    const params = new URLSearchParams()
    params.append('access_token', accessToken);
    
    await cas.doPost('https://localhost:8443/cas/oauth2.0/profile', params, {},
        function (res) {
            let result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
            assert(result.service === "https://apereo.github.io");
        }, function (error) {
            throw error;
        });

    await browser.close();
})();
