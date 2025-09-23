const cas = require("../../cas.js");
const assert = require("assert");

async function removeAllSessions() {
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete("https://localhost:8443/cas/actuator/ssoSessions?type=ALL&from=1&count=100000");
}

async function verifyTicketGrantingTicketCount(count = 0) {
    await cas.doGet("https://localhost:8443/cas/actuator/ticketRegistry/query?type=TGT", async (res) => {
        assert(res.status === 200);
        assert(res.data.length === count);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });
}

async function testCasApplication() {
    await removeAllSessions();
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.sleep(5500);

    await cas.log("The SSO session should be gone now.");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);

    await cas.log("Creating new SSO session and keeping it alive...");
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.log("Asking for service tickets to ensure SSO session remains updated...");
    for (let i = 0; i < 2; i++) {
        await cas.sleep(1000);
        await cas.gotoLogin(page, service);
        await cas.assertTicketParameter(page);
    }
    await cas.sleep(6000);
    await cas.log("The SSO session should be gone now");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.closeBrowser(browser);
    await verifyTicketGrantingTicketCount();
    await removeAllSessions();
}

async function testOidcApplication() {
    
    await removeAllSessions();
    const redirectUri = "https://localhost:9859/anything/oidc";
    const clientId = "client";
    const scope = `${encodeURIComponent("openid profile")}`;
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=${clientId}&scope=${scope}&`
        + `redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3`;

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const tokens = [];
    for (let i = 0; i < 5; i++) {
        await cas.log(`Sending OpenID Connect authentication request; attempt #${i}`);
        await cas.goto(page, url);
        await cas.sleep(1000);

        if (i === 0) {
            await cas.loginWith(page);
            await cas.sleep(1000);
        }
        if (await cas.isVisible(page, "#allow")) {
            await cas.click(page, "#allow");
            await cas.waitForNavigation(page);
        }
        await cas.sleep(1000);
        const code = await cas.assertParameter(page, "code");

        await cas.log(`Current code is ${code}`);
        const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
            + `&scope=${scope}&client_id=${clientId}&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
        const payload = await cas.doPost(accessTokenUrl, "", {
            "Content-Type": "application/json"
        }, (res) => res.data, (error) => {
            throw `Operation failed to obtain access token: ${error}`;
        });
        assert(payload.access_token !== undefined);
        assert(payload.id_token !== undefined);
        assert(payload.refresh_token !== undefined);
        tokens.push(payload);
        await cas.sleep(1000);
    }

    await cas.separator();
    await cas.log("Using refresh tokens to obtain new access tokens...");
    
    const value = `${clientId}:secret`;
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    for (const tokenObject of tokens) {
        const refreshToken = tokenObject["refresh_token"];
        await cas.log(`Using refresh token ${refreshToken}`);
        const accessTokenParams = `grant_type=refresh_token&refresh_token=${refreshToken}`;
        const accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}`;
        await cas.log(`Calling endpoint: ${accessTokenUrl}`);

        await cas.doPost(accessTokenUrl, "", {
            "Content-Type": "application/json",
            "Authorization": authzHeader
        }, (res) => {
            const result = res.data;
            assert(result.access_token !== undefined);
            assert(result.id_token !== undefined);
            assert(result.refresh_token === undefined);
        }, (error) => {
            throw error;
        });
        await cas.sleep(3000);
    }
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    
    await cas.sleep(4000);
    await cas.log("The SSO session should be gone now");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.closeBrowser(browser);
    await verifyTicketGrantingTicketCount();
    await removeAllSessions();
}

(async () => {
    await testCasApplication();
    await cas.separator();
    await testOidcApplication();
})();

