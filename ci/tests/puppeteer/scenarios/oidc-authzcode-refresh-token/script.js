const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

async function fetchRefreshToken(page, clientId, redirectUrl) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20offline_access&prompt=login&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.waitForTimeout(page, 1000);
    await cas.loginWith(page);
    await cas.waitForTimeout(page, 1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `client_id=${clientId}&client_secret=secret&grant_type=authorization_code&redirect_uri=${redirectUrl}`;
    let accessToken = null;
    let refreshToken = null;

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {"Content-Type": "application/json"},
        (res) => {
            
            assert(res.data.access_token !== undefined);
            assert(res.data.refresh_token !== undefined);

            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;

            cas.log(`Received access token ${accessToken}`);
            cas.log(`Received refresh token ${refreshToken}`);
        },
        (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

    assert(accessToken !== undefined, "Access Token cannot be null");
    assert(refreshToken !== undefined, "Refresh Token cannot be null");
    return refreshToken;
}

async function exchangeToken(refreshToken, clientId, successHandler, errorHandler) {
    let accessTokenParams = "scope=openid%offline_access";
    accessTokenParams += `&grant_type=refresh_token&refresh_token=${refreshToken}`;

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}`;
    await cas.log(`Calling endpoint: ${accessTokenUrl}`);

    const value = `${clientId}:secret`;
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": authzHeader
    }, successHandler, errorHandler);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.logg("Fetching first refresh token");
    const redirectUrl1 = "https://localhost:9859/anything/cas";
    const refreshToken1 = await fetchRefreshToken(page, "client", redirectUrl1);

    await cas.log("**********************************************");
    
    await cas.logg("Fetching second refresh token");
    const redirectUrl2 = "https://localhost:9859/anything/sample";
    const refreshToken2 = await fetchRefreshToken(page, "client2", redirectUrl2);

    await cas.logg(`Refresh Token 1: ${refreshToken1}`);
    await cas.logg(`Refresh Token 2: ${refreshToken2}`);

    await exchangeToken(refreshToken2, "client",
        (res) => {
            throw `Operation should fail but instead produced: ${res.data}`;
        }, (error) => {
            cas.log(`Status: ${error.response.status}`);
            assert(error.response.status === 400);
            cas.log(error.response.data);
            assert(error.response.data.error === "invalid_grant");
        });

    await exchangeToken(refreshToken1, "client2",
        (res) => {
            throw `Operation should fail but instead produced: ${res.data}`;
        }, (error) => {
            cas.log(`Status: ${error.response.status}`);
            assert(error.response.status === 400);
            cas.log(error.response.data);
            assert(error.response.data.error === "invalid_grant");
        });

    await exchangeToken(refreshToken1, "client",
        (res) => {
            cas.log(res.data);
            assert(res.status === 200);
        }, (error) => {
            throw `Operation should not fail but instead produced: ${error}`;
        });

    await cas.log("Let's wait 5 seconds for the TGT to expire, RTs should be still alive");
    await cas.waitForTimeout(page, 5000);

    await exchangeToken(refreshToken1, "client",
        (res) => {
            cas.log(res.data);
            assert(res.status === 200);
        }, () => {
            throw "Operation should not fail";
        });

    await exchangeToken(refreshToken2, "client2",
        (res) => {
            cas.log(res.data);
            assert(res.status === 200);
        }, () => {
            throw "Operation should not fail";
        });

    await browser.close();
})();
