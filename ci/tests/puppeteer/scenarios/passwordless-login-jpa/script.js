const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    const pswd = await page.$("#password");
    assert(pswd === null);

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerTextStartsWith(page, "#login p", "Please provide the security token sent to you");
    await cas.assertVisibility(page, "#token");

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.waitForElement(page2, "table tbody td a");
    await cas.click(page2, "table tbody td a");
    await cas.waitForElement(page2, "div[name=bodyPlainText] .well");
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.assertCookie(page);
    await browser.close();
})();
