const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.type(page, '#username', "duobypass");
    await cas.pressEnter(page);
    await cas.screenshot(page);
    await cas.log("Waiting for Duo MFA to complete...");
    await page.waitForTimeout(12000);
    await cas.screenshot(page);
    await cas.log("Checking for service ticket...");
    await cas.assertTicketParameter(page);

    await cas.log("Checking for SSO Session cookie...");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);

    await browser.close();
})();
