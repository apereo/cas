const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.type(page, '#username', "duobypass");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(10000)
    await cas.assertTicketParameter(page);
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    await browser.close();
})();
