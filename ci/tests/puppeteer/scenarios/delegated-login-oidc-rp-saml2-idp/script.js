
const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.sleep(1000);

    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);

    await cas.assertVisibility(page, "li #SAML2Client");
    await cas.click(page, "li #SAML2Client");
    await cas.sleep(3000);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.log("Checking for page URL...");
    await cas.logPage(page);
    await cas.sleep(2000);

    await cas.log("Allowing release of scopes and claims...");
    await cas.logPage(page);
    
    const result = new URL(page.url());
    await cas.log(result.searchParams.toString());

    assert(result.searchParams.has("ticket") === false);
    assert(result.searchParams.has("client_id"));
    assert(result.searchParams.has("redirect_uri"));
    assert(result.searchParams.has("scope"));

    await cas.click(page, "#allow");
    await cas.sleep(3000);
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    await cas.logPage(page);
    assert(await page.url().startsWith("https://oidcdebugger.com/debug"));
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();
