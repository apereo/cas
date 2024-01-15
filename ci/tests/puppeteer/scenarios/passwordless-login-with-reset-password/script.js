const puppeteer = require("puppeteer");
const assert = require("assert");

const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);

    const pswd = await page.$("#password");
    assert(pswd === null);

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await page.waitForTimeout(2000);

    await cas.assertInvisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.assertVisibility(page, "#forgotPasswordLink");

    await cas.click(page, "#forgotPasswordLink");
    await page.waitForTimeout(1000);

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(2000);

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);
    const pwdResetUrl =  await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.goto(pwdResetUrl);
    await page.waitForTimeout(1000);

    await cas.assertInnerText(page, "#content h3", "Hello, casuser. You must change your password.");
    await page.waitForTimeout(2000);

    await browser.close();
})();
