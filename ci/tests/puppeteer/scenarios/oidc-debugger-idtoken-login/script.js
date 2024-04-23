
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUrl = "https://localhost:9859/post";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        `redirect_uri=${redirectUrl}&` +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=id_token&" +
        "response_mode=form_post&" +
        "state=abc1234567890&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.screenshot(page);

    const responseUrl = new URL(await page.url());
    const params = new URLSearchParams(responseUrl.search);
    assert(params.getAll("").length === 0);
    const fragment = responseUrl.hash.substring(1);
    await cas.log(fragment);
    assert(fragment.includes("id_token="));
    assert(fragment.includes("nonce="));
    assert(fragment.includes("state="));
    assert(!fragment.includes("access_token="));
    await browser.close();
})();

