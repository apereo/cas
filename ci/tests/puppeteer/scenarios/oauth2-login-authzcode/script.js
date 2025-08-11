
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

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

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

    const params = new URLSearchParams();
    params.append("access_token", accessToken);
    
    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
            assert(result.service === redirectUri);
            assert(result.attributes.oauthClientId === "client");
            assert(result.attributes.organization === "apereo");
            assert(result.attributes.email === "casuser@apereo.org");
            assert(result.attributes.username === "casuser");
        }, (error) => {
            throw error;
        });

    await cas.closeBrowser(browser);
})();
