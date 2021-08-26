const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

async function fetchRefreshToken(page, clientId, redirectUrl) {
    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20offline_access&prompt=login&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    console.log(`OAuth code ${code}`);

    let accessTokenParams = `client_id=${clientId}&`;
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessToken = null;
    let refreshToken = null;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {'Content-Type': "application/json"},
        function (res) {
            console.log(res.data);
            assert(res.data.access_token !== null);
            assert(res.data.refresh_token !== null);

            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;

            console.log(`Received access token ${accessToken}`);
            console.log(`Received refresh token ${refreshToken}`);
        },
        function () {
            throw `Operation failed to obtain access token: ${error}`;
        })

    assert(accessToken != null, "Access Token cannot be null")
    assert(refreshToken != null, "Refresh Token cannot be null")
    return refreshToken;
}

async function exchangeToken(refreshToken, clientId, successHandler, errorHandler) {
    let accessTokenParams = "scope=openid%offline_access";
    accessTokenParams += `&grant_type=refresh_token&refresh_token=${refreshToken}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}`;
    console.log(`Calling endpoint: ${accessTokenUrl}`);

    let value = `${clientId}:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    console.log(`Authorization header: ${authzHeader}`);

    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json",
        'Authorization': authzHeader
    }, successHandler, errorHandler);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl1 = "https://github.com/apereo/cas";
    let refreshToken1 = await fetchRefreshToken(page, "client", redirectUrl1);

    const redirectUrl2 = "https://apereo.github.io";
    let refreshToken2 = await fetchRefreshToken(page, "client2", redirectUrl2);

    console.log(`Refresh Token 1: ${refreshToken1}`);
    console.log(`Refresh Token 2: ${refreshToken2}`);

    await exchangeToken(refreshToken2, "client",
        function (res) {
            throw `Operation should fail but instead produced: ${res.data}`;
        }, function (error) {
            console.log(`Status: ${error.response.status}`);
            assert(error.response.status === 400)
            console.log(error.response.data);
            assert(error.response.data.error === "invalid_grant");
        });

    await exchangeToken(refreshToken1, "client2",
        function (res) {
            throw `Operation should fail but instead produced: ${res.data}`;
        }, function (error) {
            console.log(`Status: ${error.response.status}`);
            assert(error.response.status === 400)
            console.log(error.response.data);
            assert(error.response.data.error === "invalid_grant");
        });

    await exchangeToken(refreshToken1, "client",
        function (res) {
            console.log(res.data);
            assert(res.status === 200);
        }, function (error) {
            throw `Operation should fail but instead produced: ${error}`;
        });

    await browser.close();
})();
