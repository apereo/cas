const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20offline_access&prompt=login&redirect_uri=https://github.com/apereo/cas&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = `client_id=client&`;
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=https://github.com/apereo/cas`;

    let accessToken = null;
    let refreshToken = null;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {'Content-Type': "application/json"},
        res => {
            
            assert(res.data.access_token !== null);
            assert(res.data.refresh_token !== null);

            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;

            cas.log(`Received access token ${accessToken}`);
            cas.log(`Received refresh token ${refreshToken}`);
        },
        () => {
            throw `Operation failed to obtain access token`;
        });

    assert(accessToken != null, "Access Token cannot be null");
    assert(refreshToken != null, "Refresh Token cannot be null");

    cas.log('Lets wait 10s for the TGT to expire, the RT should not expire based on its specific service expiration policy');
    await page.waitForTimeout(10000);

    let refreshTokenParams = "scope=openid%offline_access";
    refreshTokenParams += `&grant_type=refresh_token&refresh_token=${refreshToken}`;

    let refreshTokenUrl = `https://localhost:8443/cas/oidc/token?${refreshTokenParams}`;
    cas.log(`Calling endpoint: ${refreshTokenUrl}`);

    let value = `client:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.doPost(refreshTokenUrl, "", {
        'Content-Type': "application/json",
        'Authorization': authzHeader
    },
        res => {

            assert(res.data.access_token !== null);

            accessToken = res.data.access_token;

            cas.log(`Received new access token ${accessToken}`);
        },
        () => {
            throw `Operation failed to use refresh token`;
        });

    await browser.close();

})();
