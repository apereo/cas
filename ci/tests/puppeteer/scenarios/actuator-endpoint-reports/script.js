const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions({args: ['--incognito']}));
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    let tgc = await cas.assertCookie(page);
    await browser.close();

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
        "httpexchanges",
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
        `sso?tgc=${tgc.value}`,
        "casModules",
        "casFeatures",
        "ticketExpirationPolicies?serviceId=10000001",
        "springWebflow",
        "statistics",
        "resolveAttributes/casuser",
        "releaseAttributes?username=casuser&password=Mellon&service=https://example.com",
        "releaseAttributes?username=casuser&service=https://example.com"];

    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        await cas.log("===================================");
        await cas.log(`Trying ${url}`);
        let body = await cas.doRequest(url, "GET", {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36'
        }, 200);
        await cas.log(body);
        await cas.log("===================================");
    }

    const ticketMetrics = [
        "addTicket",
        "getTicket",
        "getTickets"
    ];
    for (let i = 0; i < ticketMetrics.length; i++) {
        let url = `${baseUrl}metrics/org.apereo.cas.ticket.registry.TicketRegistry.${ticketMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200);
    }

    const servicesMetrics = [
        "load",
        "count",
        "findServiceBy"
    ];
    for (let i = 0; i < servicesMetrics.length; i++) {
        let url = `${baseUrl}metrics/org.apereo.cas.services.ServicesManager.${servicesMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200);
    }

    const authnMetrics = [
        "authenticate"
    ];
    
    for (let i = 0; i < authnMetrics.length; i++) {
        let url = `${baseUrl}metrics/org.apereo.cas.authentication.AuthenticationManager.${authnMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }, 200);
    }
})();

