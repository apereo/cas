const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();

    await page.goto("https://localhost:8443/cas/login");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

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
        "ticketExpirationPolicies?service=10000001",
        "springWebflow",
        "statistics",
        "resolveAttributes/casuser",
        "releaseAttributes?username=casuser&password=Mellon&service=https://example.com",
        "status"];

    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        console.log("Trying " + url)
        const response = await page.goto(url);
        console.log(response.status() + " " + response.statusText())
        assert(response.ok())
    }
    await browser.close();
})();

