const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?client_id=client&"
        + "redirect_uri=https://apereo.github.io&scope=openid&state=U7yWide2Ak&nonce=8xiyRZUiYP&"
        + "response_type=code&response_mode=form_post&login_hint=casuser@localhost"
    console.log(`Navigating to ${url}`);
    await page.goto(url);

    let hint = await cas.inputValue(page, "#username")
    assert(hint === "casuser@localhost")
    await browser.close();
})();
