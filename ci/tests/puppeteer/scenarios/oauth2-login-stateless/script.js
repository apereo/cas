const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

async function verifyNormalFlows(page) {
    const redirectUri = "http://localhost:9889/anything/app";
    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=profile&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(3000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

    let accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
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

    const params = new URLSearchParams();
    params.append("access_token", accessToken);

    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
            assert(result.service === redirectUri);
            assert(result.email[0] === "casuser@apereo.org");
            assert(result.organization[0] === "apereo");
            assert(result.username[0] === "casuser");
        }, (error) => {
            throw error;
        });

    accessTokenParams = `grant_type=refresh_token&refresh_token=${refreshToken}`;
    accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}`;
    await cas.log(`Calling endpoint: ${accessTokenUrl}`);

    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": authzHeader
    }, (res) => {
        const result = res.data;
        assert(result.access_token !== undefined);
        assert(result.expires_in !== undefined);
        assert(result.token_type === "Bearer");
        assert(result.scope === "profile");
    }, (error) => {
        throw error;
    });
}

async function verifyDeviceCode(page) {
    const url = "https://localhost:8443/cas/oauth2.0/accessToken?response_type=device_code&client_id=client3";
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        assert(res.data.device_code !== undefined);
        assert(res.data.user_code !== undefined);
        assert(res.data.verification_uri !== undefined);
        assert(res.data.interval !== undefined);
        assert(res.data.expires_in !== undefined);

        await page.goto(res.data.verification_uri);
        await page.waitForTimeout(1000);
        await cas.loginWith(page);
        await cas.type(page, "#usercode", res.data.user_code);
        await cas.pressEnter(page);
        await page.waitForNavigation();
        await page.waitForTimeout(4000);
        await cas.doPost(url, "", {
            "Content-Type": "application/json"
        }, (res) => {
            assert(res.data.access_token !== undefined);
            assert(res.data.token_type !== undefined);
            assert(res.data.expires_in !== undefined);
            assert(res.data.refresh_token !== undefined);
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    }, async (error) => {
        throw `Operation failed to obtain device token: ${error}`;
    });
}

async function verifyJwtAccessToken(page) {
    const redirectUri = "http://localhost:9889/anything/jwtat";
    const url = `https://localhost:8443/cas/oauth2.0/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client2&scope=profile&state=9qa3`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(3000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    const accessTokenParams = `client_id=client2&client_secret=secret2&grant_type=authorization_code&redirect_uri=${redirectUri}`;
    const accessTokenUrl = `https://localhost:8443/cas/oauth2.0/token?${accessTokenParams}&code=${code}`;
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

    await cas.verifyJwt(accessToken, process.env.OAUTH_ACCESS_TOKEN_SIGNING_KEY, {
        algorithms: ["HS512"],
        complete: true
    });
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        let context = await browser.createIncognitoBrowserContext();
        let page = await cas.newPage(context);
        await verifyNormalFlows(page);
        await context.close();

        context = await browser.createIncognitoBrowserContext();
        page = await cas.newPage(context);
        await verifyJwtAccessToken(page);
        await context.close();

        context = await browser.createIncognitoBrowserContext();
        page = await cas.newPage(context);
        await verifyDeviceCode(page);
        await context.close();
    } finally {
        await browser.close();
    }
})();
