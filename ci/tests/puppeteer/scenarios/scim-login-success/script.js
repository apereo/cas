const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casscimuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.doGet("http://localhost:9666/scim/v2/Users?attributes=userName",
        res => {
            assert(res.status === 200)
            let length = res.data.Resources.length;
            console.log(`Found ${length} record`);
            assert(length === 1)
            assert(res.data.Resources[0].userName === "casscimuser")
        },
        error => {
            throw error;
        }, { 'Authorization': "Basic c2NpbS11c2VyOmNoYW5nZWl0" })

    
    await browser.close();
})();
