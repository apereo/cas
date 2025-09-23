
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUri = "https://localhost:9859/post";
    const url = `https://localhost:8443/cas/oidc/oidcAuthorize?state=1001&client_id=client&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent("openid profile")}&response_type=code&nonce=vn4qulthnx`;
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    const content = await cas.textContent(page, "body pre");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.code !== undefined);
    assert(payload.form.nonce === "vn4qulthnx");
    assert(payload.form.state === "1001");
    await cas.closeBrowser(browser);
})();

