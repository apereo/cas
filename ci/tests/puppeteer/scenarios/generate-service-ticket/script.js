const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Generating service ticket without SSO")
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketParameter(page);

    console.log("Generating service ticket with SSO")
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await cas.assertTicketParameter(page);
    
    await browser.close();
})();
