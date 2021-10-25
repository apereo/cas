const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    for (let i = 0; i < 5; i++) {
        await page.goto("https://localhost:8443/cas/logout");
        await page.goto("https://localhost:8443/cas/login");
        await cas.loginWith(page, "casuser", "Mellon");
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    }
    await browser.close();
})();
