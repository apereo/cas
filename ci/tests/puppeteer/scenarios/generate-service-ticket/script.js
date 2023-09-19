const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Generating service ticket without SSO");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com");
    await cas.loginWith(page);

    await cas.assertTicketParameter(page);

    await cas.log("Generating service ticket with SSO");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com");
    await cas.assertTicketParameter(page);
    
    await browser.close();
})();
