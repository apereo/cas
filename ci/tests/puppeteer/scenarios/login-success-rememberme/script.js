const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

async function loginAndVerify() {
    let browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.click(page, "#rememberMe");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    let tgc = await cas.assertCookie(page);
    let date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    let now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate());
    await browser.close();

    browser = await puppeteer.launch(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    tgc = await cas.assertCookie(page);
    date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate());
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();
}

(async () => {
    await loginAndVerify();
    await cas.refreshContext();
    await loginAndVerify();
})();
