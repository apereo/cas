
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    let url = "https://localhost:8443/cas/oidc/authorize?response_type=code&";
    url += `client_id=client&scope=${encodeURIComponent("openid email profile address phone")}&redirect_uri=${redirectUrl}`;
    url += "&nonce=3d3a7457f9ad3&state=1735fd6c43c14&acr_values=mfa-gauth";
    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrl(page, `${redirectUrl}?error=unmet_authentication_requirements`);
    await cas.closeBrowser(browser);
})();
