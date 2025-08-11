
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&"
        + `redirect_uri=${redirectUrl}&scope=openid&state=U7yWide2Ak&nonce=8xiyRZUiYP&`
        + "response_type=code";
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#username");
    await cas.closeBrowser(browser);
})();
