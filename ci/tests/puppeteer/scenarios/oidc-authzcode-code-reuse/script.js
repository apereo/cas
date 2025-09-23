
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://github.com/apereo/cas";

    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid email profile address phone")}&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        const decoded = await cas.decodeJwt(res.data.id_token);

        assert(decoded.sub !== undefined);
        assert(decoded["preferred_username"] === undefined);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined, "Access Token cannot be null");

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${accessToken}`;
    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.email !== undefined);
        assert(res.data.gender !== undefined);
        assert(res.data.name !== undefined);
        assert(res.data["preferred_username"] !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.log(`Trying to reuse OAuth code ${accessTokenUrl}`);
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, () => {
        throw `OAuth code ${code} cannot be used again`;
    }, (error) => {
        cas.log(error.response.data);
        assert(error.response.data.error === "invalid_grant");
    });

    await cas.log(`Reusing OAuth code ${code} should have revoked access token ${accessToken}`);
    await cas.log(`Calling user profile again with revoked access token: ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, () => {
        throw `Access token ${accessToken} should have been removed and rejected with code reused`;
    }, (error) => {
        assert(error.response.status === 401);
        cas.log(error.response.data);
        assert(error.response.data.error === "expired_accessToken");
    });

    await cas.closeBrowser(browser);
})();
