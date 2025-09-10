const assert = require("assert");
const cas = require("../../cas.js");

async function verifyJwtAccessToken(page, redirectUri, clientId) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=${clientId}&scope=openid&state=9qa3`;

    await cas.gotoLogout(page);
    await cas.sleep(2000);
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `scope=openid&client_id=${clientId}&client_secret=secret&grant_type=authorization_code&redirect_uri=${redirectUri}`;
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    let refreshToken = null;
    await cas.doPost(accessTokenUrl, "",
        {
            "Content-Type": "application/json"
        }, (res) => {
            assert(res.data.access_token !== undefined);
            accessToken = res.data.access_token;
            refreshToken = res.data.refresh_token;
        }, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });
    assert(accessToken !== undefined);
    assert(refreshToken !== undefined);
    return {accessToken, refreshToken};
}


async function verifyAccessTokenWithProfile(accessToken) {
    const profileUrl = "https://localhost:8443/cas/oidc/oidcProfile";
    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken}`
    }, (res) => {

    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await cas.log("Fetching access token with RSA key...");
        const result1 = await verifyJwtAccessToken(page, "http://localhost:9889/anything/app", "client");
        await cas.log("Fetching access token with EC key...");
        const result2 = await verifyJwtAccessToken(page, "http://localhost:9889/anything/app2", "client2");

        await cas.log("Verifying RSA access token with user profile endpoint...");
        await verifyAccessTokenWithProfile(result1.accessToken);
        await cas.log("Verifying EC access token with user profile endpoint...");
        await verifyAccessTokenWithProfile(result2.accessToken);

        await context.close();
    } finally {
        await browser.close();
    }
})();
