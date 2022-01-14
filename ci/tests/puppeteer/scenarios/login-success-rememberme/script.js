const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/logout");

    let now = new Date();
    await cas.logg(`Current date: ${now}`);

    await page.goto("https://localhost:8443/cas/login");
    await cas.click(page, "#rememberMe")
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    let tgc = await cas.assertTicketGrantingCookie(page);
    let date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);
    assert(now.getDay() + 1 === date.getDay())
    await browser.close();
})();
