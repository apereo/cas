const cas = require("../../cas.js");
(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&redirect_uri=https://github.com&scope=openid&state=UDWY0eSHWH&nonce=sURRPJgKkr&response_type=code&ui_locales=de";

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);

    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    
    await cas.loginWith(page);
    await cas.assertInnerText(page, "#allow", "ERLAUBEN");
    await cas.assertInnerText(page, "#cancel", "VERBIETEN");

    await cas.closeBrowser(browser);
})();
