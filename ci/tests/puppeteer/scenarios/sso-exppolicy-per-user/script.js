const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertCookie(page);
    console.log("Waiting for hard timeout to complete...");
    await page.waitForTimeout(3000);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page, false);
    await browser.close();
})();
