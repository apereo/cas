const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.waitForTimeout(page, 2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await cas.waitForTimeout(page, 2000);

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await cas.screenshot(page);

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.waitForTimeout(page2, 1000);
    await cas.click(page2, "table tbody td a");
    await cas.waitForTimeout(page2, 1000);
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page, 4000);
    await cas.screenshot(page);

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await browser.close();
})();
