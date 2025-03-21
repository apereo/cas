
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https://localhost:9859/post&" +
        `scope=${encodeURIComponent("openid email profile address phone")}&` +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await cas.log(`First attempt: navigating to ${url}`);
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    let content = await cas.textContent(page, "body");
    let payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.code !== undefined);
    assert(payload.form.nonce !== undefined);

    url = `${url}&prompt=login`;
    await cas.log(`Second attempt: navigating to ${url}`);
    await cas.goto(page, url);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    
    await cas.loginWith(page);
    await cas.sleep(2000);
    content = await cas.textContent(page, "body");
    payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.code !== undefined);
    assert(payload.form.nonce !== undefined);

    await browser.close();
})();

