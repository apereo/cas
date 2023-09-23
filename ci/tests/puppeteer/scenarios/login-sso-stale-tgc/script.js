const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    let browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    let cookie0 = await buildCookie(page, "TGT-09876", "/cas");
    let cookie1 = await buildCookie(page, "TGT-12345", "/cas/");
    let cookie2 = await buildCookie(page, "TGT-67890", "/");
    let cookie3 = await buildCookie(page, "OtherCookie", "/cas/", "TestCookie");
    await page.setCookie(cookie0, cookie1, cookie2, cookie3);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.loginWith(page);
    let tgc = await cas.assertCookie(page);
    assert(tgc.path === "/cas");
    await browser.close();

    browser = await puppeteer.launch(cas.browserOptions());
    page = await cas.newPage(browser);
    tgc.path = "/cas/";
    await page.setCookie(tgc, cookie3);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();
})();

async function buildCookie(page, value, path, name = "TGC") {
    await cas.log(`Adding cookie ${name}:${value}:${path}`);
    return {
        name: name,
        value: value,
        domain: "localhost",
        path: path,
        httpOnly: true,
        secure: true
    }
}
