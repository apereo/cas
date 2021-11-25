const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.type(page, '#username', "duobypass");
    await page.keyboard.press('Enter');
    await cas.screenshot(page);
    console.log("Waiting for Duo MFA to complete...")
    await page.waitForTimeout(12000)
    await cas.screenshot(page);
    console.log("Checking for service ticket...")
    await cas.assertTicketParameter(page);

    console.log("Checking for SSO Session cookie...")
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    await browser.close();
})();
