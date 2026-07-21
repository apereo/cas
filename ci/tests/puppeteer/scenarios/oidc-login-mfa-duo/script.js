
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await cas.updateDuoSecurityUserStatus("duocode");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogout(page);
    
    const url = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${url}`);
    const payload = await getPayload(page, url, "client1", "secret1");
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "duocode");
    assert(decoded.amr.includes("mfa"));
    await cas.closeBrowser(browser);
})();

async function getPayload(page, redirectUri, clientId, clientSecret) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${encodeURIComponent("openid profile")}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page, "duocode", "Mellon");
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    await cas.sleep(4000);
    await cas.loginDuoSecurityBypassCode(page,"duocode");
    await cas.sleep(4000);
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
