const assert = require("assert");
const cas = require("../../cas.js");

async function executeFlow(browser, redirectUri, clientId, accessTokenSecret) {
    const page = await cas.newPage(browser);

    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${encodeURIComponent(redirectUri)}&client_id=${clientId}&scope=profile&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `client_id=${clientId}&client_secret=secret&grant_type=authorization_code&redirect_uri=${encodeURIComponent(redirectUri)}`;
    const accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined);

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
    params.append("access_token", accessToken);

    await cas.log(`Calling profile endpoint with a valid access token: ${accessToken}`);
    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === clientId);
            assert(result.service === redirectUri);
        }, (error) => {
            throw error;
        });

    /*
        We create a new JWT access token from the good one with a bad payload
        to make the JWT parsing internally fail.
     */
    const parts = accessToken.split(".");
    let badAccessToken;
    if (parts.length === 3) {
        badAccessToken = `${parts[0]}.Z${parts[1]}.${parts[2]}`;
    } else {
        badAccessToken = `${parts[0]}.${parts[1]}.Z${parts[2]}.${parts[3]}.${parts[4]}`;
    }

    const badParams = new URLSearchParams();
    badParams.append("access_token", badAccessToken);

    await cas.log(`Calling profile endpoint with a bad access token: ${badAccessToken}`);
    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", badParams, {},
        () => {
            throw "Operation must fail to get the profile with a bad access token";
        }, (error) => {
            assert(error.response.status === 401);
            assert(error.response.data.error === "invalid_request");
        });

    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await executeFlow(browser, "http://localhost:9889/anything/app1","client", process.env.OAUTH_ACCESS_TOKEN_SIGNING_KEY);
    await executeFlow(browser, "http://localhost:9889/anything/app2","client2", process.env.OAUTH_ACCESS_TOKEN_ENCRYPTION_KEY);
    await cas.closeBrowser(browser);
})();
