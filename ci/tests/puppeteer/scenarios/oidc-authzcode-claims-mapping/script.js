
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUrl = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid profile email")}&redirect_uri=${redirectUrl}`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;

    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== undefined);
    assert(decoded.client_id !== undefined);
    assert(decoded["preferred_username"] === "apereo-casuser");
    assert(decoded["email"] !== undefined);
    assert(decoded["family_name"] !== undefined);
    assert(decoded["given_name"] !== undefined);
    assert(decoded["name"] !== undefined);
    await cas.closeBrowser(browser);
})();
