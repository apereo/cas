const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

async function testService(page, clientId, oidc = true) {
    await cas.log(`Testing application with client id ${clientId}`);
    const redirectUrl = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${encodeURIComponent("openid profile")}&redirect_uri=${redirectUrl}`;
    await cas.goto(page, url);

    await cas.loginWith(page);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code&client_id=${clientId}&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);

    await cas.log("Decoding access token...");
    const decodedAccessToken = await cas.decodeJwt(payload.access_token);

    if (oidc) {
        assert(decodedAccessToken.iss === "https://sso.example.org/cas/oidc");
        await cas.log("Decoding ID token...");
        assert(payload.id_token !== undefined);
        const decodedIdToken = await cas.decodeJwt(payload.id_token);
        assert(decodedIdToken.sub !== undefined);
        assert(decodedIdToken.client_id !== undefined);
        assert(decodedIdToken.iss === "https://sso.example.org/cas/oidc");
    } else {
        assert(decodedAccessToken.grant_type === "authorization_code");
        assert(decodedAccessToken.iss === "https://localhost:8443/cas/oidc");
        assert(decodedAccessToken.client_id === "oauth-clientid");
    }
    
    await cas.gotoLogout(page);
    await cas.log("=========================================================");
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await testService(page, "client", true);
    await testService(page, "oauth-clientid", false);
    await browser.close();
})();
