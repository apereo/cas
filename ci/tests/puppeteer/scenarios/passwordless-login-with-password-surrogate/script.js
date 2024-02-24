const puppeteer = require("puppeteer");
const assert = require("assert");

const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    const pswd = await page.$("#password");
    assert(pswd === null);

    await cas.type(page,"#username", "user3+casuser");
    await cas.pressEnter(page);

    await cas.waitForTimeout(page);

    await cas.assertInvisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await cas.type(page,"#password", "Mellon");
    await cas.pressEnter(page);

    await cas.waitForTimeout(page);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await cas.waitForTimeout(page);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await cas.waitForTimeout(page);
    await cas.screenshot(page);
    
    await cas.assertInnerTextStartsWith(page, "#surrogateEnabled td code kbd", "[true]");
    await cas.assertInnerTextStartsWith(page, "#surrogatePrincipal td code kbd", "[casuser]");
    await cas.assertInnerTextStartsWith(page, "#surrogateUser td code kbd", "[user3]");
    await cas.waitForTimeout(page);

    await browser.close();
})();
