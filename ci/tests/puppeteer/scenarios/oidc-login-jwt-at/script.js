const assert = require("assert");
const cas = require("../../cas.js");

async function verifyJwtAccessToken(page) {
    const redirectUri = "http://localhost:9889/anything/app";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=openid&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `scope=openid&client_id=client&client_secret=secret&grant_type=authorization_code&redirect_uri=${redirectUri}`;
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
    
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        (res) => {
            assert(res.status === 200);
            cas.log(`Using key identifier ${res.data.keys[0]["kid"]}`);

            cas.verifyJwtWithJwk(accessToken, res.data.keys[0], "RS512").then((verified) => {
                cas.log(verified);
                assert(verified.payload.aud === "client");
                assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                assert(verified.payload.sub === "casuser");
                assert(verified.payload.email === "casuser@apereo.org");
                assert(verified.payload.organization === "apereo");
                assert(verified.payload.exp !== undefined);
                assert(verified.payload.iat !== undefined);
                assert(verified.payload.jti !== undefined);
            });
        },
        (error) => {
            throw error;
        });
    
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await verifyJwtAccessToken(page);
        await context.close();
    } finally {
        await cas.closeBrowser(browser);
    }
})();
