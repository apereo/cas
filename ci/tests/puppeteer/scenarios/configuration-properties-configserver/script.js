const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {

    await cas.doGet("http://casuser:Mellon@localhost:8888/casconfigserver/cas/dev",
        res => {
            assert(res.status === 200)
        }, err => {
            throw err;
        })
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.loginWith(page, "configserver", "p@SSword");
    await cas.assertTicketParameter(page);
    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await page.goto("https://localhost:8443/cas/logout");
    await browser.close();
})();
