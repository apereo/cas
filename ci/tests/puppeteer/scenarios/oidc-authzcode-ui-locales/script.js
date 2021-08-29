const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&redirect_uri=https://github.com&scope=openid&state=UDWY0eSHWH&nonce=sURRPJgKkr&response_type=code&ui_locales=de";

    console.log(`Navigating to ${url}`);
    await page.goto(url);

    await cas.assertInnerText(page, "#content #fm1 button[name=submit]", "ANMELDEN")
    
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertInnerText(page, "#allow", "ERLAUBEN")
    await cas.assertInnerText(page, "#cancel", "VERBIETEN")

    await browser.close();
})();
