const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "securecas", "paSSw0rd");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.doGet("https://casuser:Mellon@localhost:8443/cas/actuator/info",
        res => assert(res.status === 200), err => {
            throw err;
        });

    await browser.close();
})();
