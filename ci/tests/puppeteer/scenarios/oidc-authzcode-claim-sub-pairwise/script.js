
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url1 = "https://localhost:9859/anything/sample";
    await cas.logg(`Trying with URL ${url1}`);
    const payload = await getPayload(page, url1, "client", "secret");
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "H93BJUD2wLReU5cflwSI4Flle/U=");
    await browser.close();
})();

async function getPayload(page, redirectUri, clientId, clientSecret) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${encodeURIComponent("openid profile email")}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=${clientId}&client_secret=${clientSecret}&redirect_uri=${redirectUri}&code=${code}`;
    return cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}
