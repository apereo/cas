
const cas = require("../../cas.js");
const assert = require("assert");

const redirectUrl = "https://localhost:9859/anything/cas";

async function fetchCode(page) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid offline_access")}&prompt=login&redirect_uri=${redirectUrl}`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
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

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.doPost(accessTokenUrl, "", {"Content-Type": "application/json"},
        (res) => {
            cas.log(res.data);
            assert(res.data.access_token !== undefined);
            assert(res.data.refresh_token !== undefined);

            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;

            cas.logg(`Received access token ${accessToken}`);
            cas.logg(`Received refresh token ${refreshToken}`);
        },
        (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });

    assert(accessToken !== undefined, "Access Token cannot be null");
    assert(refreshToken !== undefined, "Refresh Token cannot be null");
    return {
        accessToken: accessToken,
        refreshToken: refreshToken
    };
}

async function fetchProfile(accessToken) {
    const params = new URLSearchParams();
    params.append("access_token", accessToken);

    await cas.log(`Getting user profile for access token ${accessToken}...`);
    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
        }, (error) => {
            throw error;
        });
}

async function refreshTokens(refreshToken, clientId, successHandler, errorHandler) {
    let accessTokenParams = `scope=${encodeURIComponent("openid offline_access")}`;
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
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const code = await fetchCode(page);
    const tokens = await exchangeCode(page, code, "client");
    await fetchProfile(tokens.accessToken);
    await refreshTokens(tokens.refreshToken, "client",
        (res) => assert(res.status === 200), (error) => {
            throw `Operation should fail but instead produced: ${error}`;
        });

    await cas.logg("Logging out, removing all tokens...");
    await cas.gotoLogout(page);
    let failed = false;
    try {
        await exchangeCode(page, code, "client");
    } catch(e) {
        await cas.logg(`Access token request has failed, correctly: ${e}`);
        failed = true;
    }
    if (!failed) {
        throw `Request should not pass; ${code} is expired`;
    }

    failed = false;
    try {
        await fetchProfile(tokens.accessToken);
    } catch (e) {
        await cas.logg(`User profile request has failed, correctly ${e}`);
        failed = true;
    }
    if (!failed) {
        throw `Profile request should not pass; ${tokens.accessToken} is expired`;
    }

    await refreshTokens(tokens.refreshToken, "client",
        () => {
            throw `Refresh Token request should not pass; ${tokens.accessToken} is expired`;
        }, () => cas.logg("Refresh Token request has failed, correctly."));

    await cas.closeBrowser(browser);
})();
