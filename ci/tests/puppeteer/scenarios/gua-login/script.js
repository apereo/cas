const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")));
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")));
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")));
    await cas.type(page,'#username', "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertTextContent(page, "#login h2", "casuser");
    await cas.assertTextContent(page, "#guaInfo", "If you do not recognize this image as yours, do NOT continue.");
    await cas.assertVisibility(page, '#guaImage');
    await cas.submitForm(page, "#fm1");
    await cas.type(page,'#password', "Mellon");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertCookie(page);
    await browser.close();
})();
