const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&"
        + "redirect_uri=https://apereo.github.io&scope=openid&state=U7yWide2Ak&nonce=8xiyRZUiYP&"
        + "response_type=code";

    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(10000)
    // wait 10s before login again, the time for the TGT to be expired
    await page.goto(url);
    await page.waitForTimeout(1000)

    // the TGT being expired (while the web session is not), the login page is displayed
    await cas.assertVisibility(page, '#username')
    await browser.close();
})();
