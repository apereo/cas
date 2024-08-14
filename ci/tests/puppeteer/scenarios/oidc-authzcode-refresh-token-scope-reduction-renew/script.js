
const cas = require("../../cas.js");
const assert = require("assert");

async function fetchRefreshToken(page, clientId, redirectUrl) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20offline_access%20email&prompt=login&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.screenshot(page);
    await cas.sleep(1000);
    await cas.logPage(page);
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

async function exchangeToken(refreshToken, clientId, scopes, successHandler, errorHandler) {
    const accessTokenParams = `scope=${scopes}&grant_type=refresh_token&refresh_token=${refreshToken}`;

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

async function introspect(token, scopes) {
    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => {
            cas.log({"introspect": res.data, "scopes": scopes});
            assert(res.data.active === true);
            assert(res.data.aud === "client");
            assert(res.data.sub === "casuser");
            assert(res.data.iss === "https://localhost:8443/cas/oidc");
            assert(res.data.client_id === "client");
            assert(res.data.token === token);
            assert(res.data.scope === scopes);
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.logg("Fetching refresh token");
    const redirectUrl = "https://localhost:9859/anything/cas";
    let refreshToken = await fetchRefreshToken(page, "client", redirectUrl);

    await cas.logg(`Refresh Token: ${refreshToken}`);

    await cas.logg("Reducing scope to 'openid offline_access'");
    await exchangeToken(refreshToken, "client", "openid%20offline_access",
        (res) => {
            cas.log({"reduced": res.data});
            assert(res.status === 200);
            refreshToken = res.data.refresh_token;
            introspect(res.data.access_token, "openid offline_access");
        }, (error) => {
            throw `Operation should not fail but instead produced: ${error}`;
        });

    await cas.logg("Request with empty scope");
    await exchangeToken(refreshToken, "client", "",
        (res) => {
            cas.log({"empty": res.data});
            assert(res.status === 200);
            refreshToken = res.data.refresh_token;
            introspect(res.data.access_token, "email openid offline_access");
        }, (error) => {
            throw `Operation should not fail but instead produced: ${error}`;
        });

    await cas.logg("Requesting original scopes");
    await exchangeToken(refreshToken, "client", "openid%20offline_access%20email",
        (res) => {
            cas.log({"original": res.data});
            assert(res.status === 200);
            refreshToken = res.data.refresh_token;
            introspect(res.data.access_token, "email openid offline_access");
        }, (error) => {
            throw `Operation should not fail but instead produced: ${error}`;
        });

    await cas.logg("Requesting additional scope");
    await exchangeToken(refreshToken, "client", "openid%20offline_acces%20profile",
        (res) => {
            throw `Operation should fail but instead produced: ${res.data}`;
        }, (error) => {
            cas.log(`Status: ${error.response.status}`);
            assert(error.response.status === 400);
            cas.log(error.response.data);
            assert(error.response.data.error === "invalid_scope");
        });

    await browser.close();
})();
