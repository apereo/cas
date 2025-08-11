
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&"
        + "redirect_uri=https://localhost:9859/anything/cas&scope=openid&state=U7yWide2Ak&nonce=8xiyRZUiYP&"
        + "response_type=code&response_mode=form_post&login_hint=casuser@localhost";
    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);

    await cas.assertInputValue(page, "#username", "casuser@localhost");
    await cas.closeBrowser(browser);
})();
