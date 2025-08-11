
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        `scope=${encodeURIComponent("openid email profile address phone")}&` +
        "response_type=code&" +
        "response_mode=fragment&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.logPage(page);
    const result = await page.evaluate(() => window.location.hash);
    assert(result.includes("code="));
    assert(result.includes("nonce="));
    await cas.sleep(1000);
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    await cas.closeBrowser(browser);
})();

