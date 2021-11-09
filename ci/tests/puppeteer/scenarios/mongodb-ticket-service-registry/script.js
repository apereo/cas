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
            assert(res.data.components.mongo !== null);
            assert(res.data.components.memory !== null);
            assert(res.data.components.ping !== null);

            assert(res.data.components.mongo.status !== null);
            assert(res.data.components.mongo.details !== null);

            let details = res.data.components.mongo.details["MongoDbHealthIndicator-ticket-registry"]
            assert(details.name === "MongoDbHealthIndicator-ticket-registry");
            assert(details.proxyGrantingTicketsCache !== null);
            assert(details.ticketGrantingTicketsCache !== null);
            assert(details.proxyTicketsCache !== null);
            assert(details.serviceTicketsCache !== null);
            assert(details.transientSessionTicketsCache !== null)

            details = res.data.components.mongo.details["MongoDbHealthIndicator-service-registry"]
            assert(details.name === "MongoDbHealthIndicator-service-registry");

        }, function (error) {
            throw error;
        }, { 'Content-Type': "application/json" })
    await browser.close();
})();
