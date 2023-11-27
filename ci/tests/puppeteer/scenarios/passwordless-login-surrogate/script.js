const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);
    await page.waitForTimeout(1000);

    let pswd = await page.$('#password');
    assert(pswd == null);

    await cas.type(page,'#username', "user3+casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(3000);
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerTextStartsWith(page, "#login p", "Please provide the security token sent to you");
    await cas.assertVisibility(page, '#token');
    await page.waitForTimeout(1000);
    
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
    await page.waitForTimeout(1000);

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await page.waitForTimeout(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    
    await cas.assertInnerTextStartsWith(page, "#surrogateEnabled td code kbd", "[true]");
    await cas.assertInnerTextStartsWith(page, "#surrogatePrincipal td code kbd", "[casuser]");
    await cas.assertInnerTextStartsWith(page, "#surrogateUser td code kbd", "[user3]");
    await page.waitForTimeout(1000);

    await browser.close();
})();
