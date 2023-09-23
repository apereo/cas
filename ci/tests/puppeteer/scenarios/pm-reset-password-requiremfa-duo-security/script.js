const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await page.waitForTimeout(2000);
    await cas.type(page,'#username', "duobypass");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(8000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");
    await browser.close();
})();
