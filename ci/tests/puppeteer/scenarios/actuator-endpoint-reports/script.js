
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    const tgc = await cas.assertCookie(page);

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
        "mfaDevices/casuser",
        "loggingConfig",
        "registeredServices",
        "registeredServices/10000001",
        "authenticationHandlers",
        "authenticationHandlers/STATIC",
        "authenticationPolicies",
        "auditLog?interval=PT1H",
        "ssoSessions",
        `sso?tgc=${tgc.value}`,
        "casModules",
        "casFeatures",
        "ticketExpirationPolicies?serviceId=10000001",
        "serviceAccess?username=casuser&password=Mellon&service=https://example.com",
        "serviceAccess?username=casuser&service=https://example.com",
        "springWebflow",
        "statistics",
        "resolveAttributes/casuser",
        "releaseAttributes?username=casuser&password=Mellon&service=https://example.com",
        "releaseAttributes?username=casuser&service=https://example.com"];

    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        const url = baseUrl + endpoints[i];
        await cas.log("===================================");
        await cas.log(`Trying ${url}`);

        const method = url.includes("serviceAccess?") || url.includes("releaseAttributes?") ? "POST" : "GET";
        const body = await cas.doRequest(url, method, {
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"
        }, 200);
        await cas.log(body);
        await cas.log("===================================");
    }

    const ticketMetrics = [
        "addTicket",
        "getTicket",
        "stream"
    ];
    for (let i = 0; i < ticketMetrics.length; i++) {
        const url = `${baseUrl}metrics/org.apereo.cas.ticket.registry.TicketRegistry.${ticketMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }

    const servicesMetrics = [
        "load",
        "count",
        "findServiceBy"
    ];
    for (let i = 0; i < servicesMetrics.length; i++) {
        const url = `${baseUrl}metrics/org.apereo.cas.services.ServicesManager.${servicesMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }

    const authnMetrics = [
        "authenticate"
    ];

    for (let i = 0; i < authnMetrics.length; i++) {
        const url = `${baseUrl}metrics/org.apereo.cas.authentication.AuthenticationManager.${authnMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }

    await cas.gotoLogout(page);

    const webflowMetrics = [
        "login",
        "logout"
    ];
    for (let i = 0; i < webflowMetrics.length; i++) {
        const url = `${baseUrl}metrics/org.springframework.webflow.executor.FlowExecutor.${webflowMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }
    
    await context.close();
    await cas.closeBrowser(browser);
})();

