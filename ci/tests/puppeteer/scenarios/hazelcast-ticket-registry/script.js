const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.waitForTimeout(page, 1000);
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        (res) => {
            assert(res.data.components.hazelcast !== undefined);
            assert(res.data.components.memory !== undefined);
            assert(res.data.components.ping !== undefined);

            assert(res.data.components.hazelcast.status !== undefined);
            assert(res.data.components.hazelcast.details !== undefined);

            const details = res.data.components.hazelcast.details;
            assert(details.name === "HazelcastHealthIndicator");
            assert(details.proxyGrantingTicketsCache !== undefined);
            assert(details.ticketGrantingTicketsCache !== undefined);
            assert(details.proxyTicketsCache !== undefined);
            assert(details.serviceTicketsCache !== undefined);
            assert(details.transientSessionTicketsCache !== undefined);
        }, (error) => {
            throw error;
        }, { "Content-Type": "application/json" });

    await cas.goto(page, "https://localhost:8444/cas/login");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await browser.close();
})();
