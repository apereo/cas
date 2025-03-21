
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https://localhost:9859/post&" +
        `scope=${encodeURIComponent("openid email profile address phone")}&` +
        "response_type=id_token%20token&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    assert(payload.form.access_token !== undefined);
    assert(payload.form.id_token !== undefined);
    assert(payload.form.token_type !== undefined);
    assert(payload.form.expires_in !== undefined);
    await browser.close();
})();
