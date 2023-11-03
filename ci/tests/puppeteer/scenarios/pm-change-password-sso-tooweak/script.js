const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:8443/cas/login?locale=en&doChangePassword=true");
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.type(page,'#password', "123456");
    await cas.type(page,'#confirmedPassword', "123456");
    let submit = await page.$('#submit');
    let disabled = await submit.evaluate(el => el.getAttribute("disabled"));
    assert("" === disabled);
    await browser.close();
})();
