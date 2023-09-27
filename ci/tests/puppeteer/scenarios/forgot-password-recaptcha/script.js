const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await page.waitForTimeout(2000);
    await cas.assertTextContent(page, "#forgotPasswordLink", "Reset your password");

    await cas.click(page, "#forgotPasswordLink");
    await page.waitForTimeout(1000);

    await cas.assertTextContent(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, '#username');
    await cas.type(page,'#username', "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    await cas.assertTextContent(page, "div .banner-danger p", "reCAPTCHA validation failed.");
    await browser.close();
})();

