const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    
    await cas.click(page, "#forgotPasswordLink");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, '#username');
    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")));
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")));
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")));

    await cas.type(page,'#username', "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await page.waitForTimeout(1000);

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await cas.goto(page, "http://localhost:8282");
    await page.waitForTimeout(1000);
    await cas.click(page, "table tbody td a");
    await page.waitForTimeout(1000);

    let link = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.goto(page, link);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content #pwdmain h3", "Hello, casuser. You must change your password.");

    await page.waitForTimeout(2000);
    await cas.goto(page, link);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content #pwdmain h3", "Hello, casuser. You must change your password.");

    await page.waitForTimeout(2000);
    await cas.goto(page, link);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#main-content h2", "Password Reset Failed");

    await page.waitForTimeout(2000);
    await browser.close();
})();
