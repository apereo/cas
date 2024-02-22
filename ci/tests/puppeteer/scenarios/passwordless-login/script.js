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
    await cas.click(page2, "table tbody td a");
    await cas.waitForElement(page2, "div[name=bodyPlainText] .well");
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");

    await cas.click(page, "#auth-tab");

    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");

    await cas.screenshot(page);
    
    const surrogateEnabled = await page.$("#surrogateEnabled");
    assert(surrogateEnabled === null);
    const surrogatePrincipal = await page.$("#surrogatePrincipal");
    assert(surrogatePrincipal === null);
    const surrogateUser = await page.$("#surrogateUser");
    assert(surrogateUser === null);

    await browser.close();
})();
