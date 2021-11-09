const puppeteer = require('puppeteer');


const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "user3+casuser", "Mellon");
    
    // await page.waitForTimeout(2000)

    await cas.assertVisibility(page, '#token')
    await browser.close();
})();
