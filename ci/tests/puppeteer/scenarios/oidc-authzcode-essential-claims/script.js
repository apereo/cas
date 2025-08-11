
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://apereo.github.io";
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&redirect_uri=${redirectUrl}&scope=openid&state=gKK1AT6qfk&nonce=gzpjHPGJpu&response_type=code&claims=%7B%22userinfo%22:%7B%22name%22:%7B%22essential%22:true%7D%7D%7D`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);
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
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);
        await cas.decodeJwt(res.data.id_token);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined, "Access Token cannot be null");

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${accessToken}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        cas.log(res.data);
        assert(res.data.name !== undefined);
        assert(res.data.sub !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.closeBrowser(browser);
})();
