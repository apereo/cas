const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    let pswd = await page.$('#password');
    assert(pswd == null);

    let uid = await page.$('#username');
    assert(await uid.evaluate(el => el.getAttribute("autocapitalize")) === "none");
    assert(await uid.evaluate(el => el.getAttribute("spellcheck")) === "false");
    assert(await uid.evaluate(el => el.getAttribute("autocomplete")) === "username");

    await cas.type(page,'#username', "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerTextStartsWith(page, "#login p", "Please provide the security token sent to you");
    await cas.assertVisibility(page, '#token');

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);
    let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(3000);
    await cas.assertCookie(page);
    await browser.close();
})();
