const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();

    await cas.doPost("https://localhost:8443/cas/actuator/auditLog", {}, {
        'Content-Type': 'application/json'
    }, res => {
        assert(res.data.length === 4);
        assert(res.data[0].principal !== null)
        assert(res.data[0].actionPerformed !== null)
        assert(res.data[0].applicationCode !== null)
        assert(res.data[0].clientIpAddress !== null)
        assert(res.data[0].serverIpAddress !== null)
        assert(res.data[0].resourceOperatedUpon !== null)
    }, error => {
        throw(error);
    })

    await cas.doGet("https://localhost:8443/cas/actuator/auditevents",
        res => {
            assert(res.data.events.length === 4);
            assert(res.data.events[0].principal !== null)
            assert(res.data.events[0].timestamp !== null)
            assert(res.data.events[0].type !== null)
            assert(res.data.events[0].data.source !== null)
        }, err => {
            throw(err);
        })
})();
