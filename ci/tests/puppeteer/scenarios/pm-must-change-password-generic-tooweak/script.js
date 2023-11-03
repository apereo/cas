const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await cas.loginWith(page, "mustchangepswd", "mustchangepswd");
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepswd. You must change your password.");
    await cas.type(page,'#password', "123456");
    await cas.type(page,'#confirmedPassword', "123456");
    var submit = await page.$('#submit');
    var disabled = await submit.evaluate(el => el.getAttribute("disabled"));
    assert("" === disabled);
    await browser.close();
})();
