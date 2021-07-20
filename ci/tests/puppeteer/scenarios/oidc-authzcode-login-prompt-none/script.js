const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/logout");
    
    const state = "40W6nJCYWnnPplmAo13Icy";
    let authzUrl = "https://localhost:8443/cas/oidc/authorize?";
    authzUrl += "response_type=code&client_id=client&scope=openid";
    authzUrl += "&redirect_uri=https://www.google.fr&nonce=yYxIingpZy&state=" + state;

    const url = authzUrl + '&prompt=none';
    await page.goto(url);

    await page.waitForTimeout(2000)

    assert(page.url() === 'https://www.google.fr/?error=login_required&state=40W6nJCYWnnPplmAo13Icy');
    let result = new URL(page.url());
    assert(result.searchParams.has("error"));
    assert(result.searchParams.get("error") === "login_required")
    assert(result.searchParams.has("state"));
    assert(result.searchParams.get("state") === state)
    
    await page.goto(authzUrl);
    await page.waitForTimeout(2000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000)

    await browser.close();
})();
