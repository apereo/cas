
const assert = require("assert");
const cas = require("../../cas.js");

async function verifyNormalFlows(page) {
    const redirectUri = "http://localhost:9889/anything/app";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=profile%20openid&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(3000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&client_secret=secret&grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    let refreshToken = null;
    await cas.doPost(accessTokenUrl, "", {
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

    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting access token ${accessToken}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${accessToken}`,
        (res) => assert(res.data.active === true), (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });

    await cas.log(`Introspecting refresh token ${refreshToken}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${refreshToken}`,
        (res) => assert(res.data.active === true), (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });

    const params = new URLSearchParams();
    params.append("access_token", accessToken);

    await cas.doPost("https://localhost:8443/cas/oidc/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.sub === "casuser");
            assert(result.client_id === "client");
            assert(result.service === redirectUri);
            assert(result.email === "casuser@apereo.org");
            assert(result.organization === "apereo");
        }, (error) => {
            throw error;
        });

    accessTokenParams = `grant_type=refresh_token&refresh_token=${refreshToken}`;
    accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}`;
    await cas.log(`Calling endpoint: ${accessTokenUrl}`);

    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": authzHeader
    }, (res) => {
        const result = res.data;
        assert(result.access_token !== undefined);
        assert(result.expires_in !== undefined);
        assert(result.token_type === "Bearer");
        assert(result.scope === "openid profile");
    }, (error) => {
        throw error;
    });
}

async function verifyJwtAccessToken(page) {
    const redirectUri = "http://localhost:9889/anything/jwtat";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client2&scope=profile%20openid&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `client_id=client2&client_secret=secret2&grant_type=authorization_code&redirect_uri=${redirectUri}`;
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
            assert(res.data.keys[0]["kid"] !== undefined);
            cas.log(`Using key identifier ${res.data.keys[0]["kid"]}`);

            cas.verifyJwtWithJwk(accessToken, res.data.keys[0], "RS512").then((verified) => {
                // await cas.log(verified)
                assert(verified.payload.sub === "casuser");
                assert(verified.payload.aud === "client2");
                assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                assert(verified.payload.state === undefined);
                assert(verified.payload.nonce === undefined);
                assert(verified.payload.iat !== undefined);
                assert(verified.payload.jti !== undefined);
                assert(verified.payload.exp !== undefined);
                assert(verified.payload.email === "casuser@apereo.org");
                assert(verified.payload.organization === "apereo");
            });
        },
        (error) => {
            throw error;
        });

}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        let context = await browser.createBrowserContext();
        let page = await cas.newPage(context);
        await verifyNormalFlows(page);
        await context.close();

        context = await browser.createBrowserContext();
        page = await cas.newPage(context);
        await verifyJwtAccessToken(page);
        await context.close();
    } finally {
        await cas.closeBrowser(browser);
    }
})();
