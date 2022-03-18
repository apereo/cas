const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(1000)
    await cas.assertTicketParameter(page)

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertTicketGrantingCookie(page);

    await browser.close();
})();
