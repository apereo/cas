
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUri = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=profile&state=9qa3`;
    
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

    const accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/x-www-form-urlencoded",
        // for basic auth in OAuth (RFC 6749 as opposed to RFC 7617, regular basic auth),
        // client and secrets are URL encoded before being base 64 encoded
        "Authorization": `Basic ${btoa("client:my%2Bsecret")}`
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined);

    await cas.closeBrowser(browser);
})();
