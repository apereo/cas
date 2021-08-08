const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await page.goto("https://localhost:8443/cas/login?service=https://example.com");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    await page.goto("https://localhost:8443/cas/login?service=https://example.com&renew=true");
    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#existingSsoMsg')

    await browser.close();
})();
