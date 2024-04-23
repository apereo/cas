
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
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
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    url = `${url}&prompt=login`;
    await cas.log(`Second attempt: navigating to ${url}`);
    await cas.goto(page, url);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    await browser.close();
})();

