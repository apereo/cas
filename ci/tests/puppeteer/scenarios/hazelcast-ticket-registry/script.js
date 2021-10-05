const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.goto("https://localhost:8443/cas/actuator/health");
    await page.waitForTimeout(1000)
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        function (res) {
            assert(res.data.components.hazelcast !== null);
            assert(res.data.components.memory !== null);
            assert(res.data.components.ping !== null);

            assert(res.data.components.hazelcast.status !== null);
            assert(res.data.components.hazelcast.details !== null);

            let details = res.data.components.hazelcast.details;
            assert(details.name === "HazelcastHealthIndicator");
            assert(details.proxyGrantingTicketsCache !== null);
            assert(details.ticketGrantingTicketsCache !== null);
            assert(details.proxyTicketsCache !== null);
            assert(details.serviceTicketsCache !== null);
            assert(details.transientSessionTicketsCache !== null);
        }, function (error) {
            throw error;
        }, { 'Content-Type': "application/json" })
    await browser.close();
})();
