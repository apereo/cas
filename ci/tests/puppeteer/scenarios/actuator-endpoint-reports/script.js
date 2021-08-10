const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);

    const endpoints = [
        "info",
        "health",
        "beans",
        "caches",
        "conditions",
        "configprops",
        "env",
        "metrics",
        "mappings",
        "loggers",
        "httptrace",
        "scheduledtasks",

        "configurationMetadata",
        "events",
        "loggingConfig",
        "registeredServices",
        "registeredServices/10000001",
        "authenticationHandlers",
        "authenticationHandlers/STATIC",
        "authenticationPolicies",
        "auditLog/PT1H",
        "ssoSessions",
        "sso",
        "casModules",
        "ticketExpirationPolicies?serviceId=10000001",
        "springWebflow",
        "statistics",
        "resolveAttributes/casuser",
        "releaseAttributes?username=casuser&password=Mellon&service=https://example.com",
        "status"];

    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        console.log(`Trying ${url}`)
        const response = await page.goto(url);
        console.log(`${response.status()} ${response.statusText()}`)
        assert(response.ok())
    }
    await browser.close();
})();

