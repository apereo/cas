const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&redirect_uri=https://github.com&scope=openid&state=UDWY0eSHWH&nonce=sURRPJgKkr&response_type=code&ui_locales=de";

    console.log("Navigating to " + url);
    await page.goto(url);

    const header = await cas.innerText(page, '#content #fm1 button[name=submit]');
    assert(header === "ANMELDEN")
    
    await cas.loginWith(page, "casuser", "Mellon");

    let text = await cas.innerText(page, '#allow');
    assert(text === "ERLAUBEN")

    text = await cas.innerText(page, '#cancel');
    assert(text === "VERBIETEN")

    await browser.close();
})();
