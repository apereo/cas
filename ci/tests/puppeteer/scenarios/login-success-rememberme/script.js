const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/logout");

    await page.goto("https://localhost:8443/cas/login");
    await cas.click(page, "#rememberMe")
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    let tgc = await cas.assertTicketGrantingCookie(page);
    let date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    let now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate())
    await page.close()

    page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    tgc = await cas.assertTicketGrantingCookie(page);
    date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate())
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await browser.close();
})();
