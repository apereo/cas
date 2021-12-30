const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    const baseUrl = "https://localhost:8443/cas/actuator/"
    await cas.doGet(`${baseUrl}loggers`,
        res => {
            assert(res.data.loggers.ROOT.configuredLevel === "OFF");
            assert(res.data.loggers['org.apereo.cas'].configuredLevel === "INFO");
        },
        err => {
            throw err;
        });
    await browser.close();
})();
