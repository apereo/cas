const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

const redirectUrl = "https://github.com/apereo/cas";

async function fetchCode(page) {
    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20offline_access&prompt=login&redirect_uri=${redirectUrl}`;

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
    await cas.logg(`OAuth code ${code}`);
    return code;
}

async function exchangeCode(page, code, clientId) {
    let accessTokenParams = `client_id=${clientId}&`;
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessToken = null;
    let refreshToken = null;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {'Content-Type': "application/json"},
        res => {
            console.log(res.data);
            assert(res.data.access_token !== null);
            assert(res.data.refresh_token !== null);

            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;

            cas.logg(`Received access token ${accessToken}`);
            cas.logg(`Received refresh token ${refreshToken}`);
        },
        error => {
            throw `Operation failed to obtain access token: ${error}`;
        })

    assert(accessToken != null, "Access Token cannot be null")
    assert(refreshToken != null, "Refresh Token cannot be null")
    return {
        accessToken: accessToken,
        refreshToken: refreshToken
    };
}

async function fetchProfile(accessToken) {
    const params = new URLSearchParams()
    params.append('access_token', accessToken);

    await cas.doPost('https://localhost:8443/cas/oauth2.0/profile', params, {},
        res => {
            let result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
        }, error => {
            throw error;
        });
}

async function refreshTokens(refreshToken, clientId, successHandler, errorHandler) {
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

    let code = await fetchCode(page);
    let tokens = await exchangeCode(page, code, "client");
    await fetchProfile(tokens.accessToken);
    await refreshTokens(tokens.refreshToken, "client",
        res => {
            assert(res.status === 200);
        }, error => {
            throw `Operation should fail but instead produced: ${error}`;
        });


    await cas.logg("Logging out, removing all tokens...")
    await page.goto("https://localhost:8443/cas/logout");
    try {
        await exchangeCode(page, code, "client");
        throw `Request should not pass; ${code} is expired`
    } catch(e) {
        await cas.logg("Access token request has failed, correctly.")
    }

    try {
        await fetchProfile(tokens.accessToken);
        throw `Profile request should not pass; ${tokens.accessToken} is expired`
    } catch (e) {
        await cas.logg("User profile request has failed, correctly.")
    }

    await refreshTokens(tokens.refreshToken, "client",
        res => {
            throw `Refresh Token request should not pass; ${tokens.accessToken} is expired`
        }, error => {
            cas.logg("Refresh Token request has failed, correctly.")
        });

    await browser.close();
})();
