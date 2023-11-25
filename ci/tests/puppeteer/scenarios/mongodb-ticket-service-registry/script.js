const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.logg("Removing all SSO Sessions");
    await cas.doRequest(`${baseUrl}/ssoSessions?type=ALL&from=1&count=100000`, "DELETE", {});

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await page.waitForTimeout(1000);
    await browser.close();
    
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        async res => {
            assert(res.data.components.mongo !== null);
            assert(res.data.components.memory !== null);
            assert(res.data.components.ping !== null);

            assert(res.data.components.mongo.status !== null);
            assert(res.data.components.mongo.details !== null);


            let details = res.data.components.mongo.details["MongoDbHealthIndicator-ticket-registry"];
            assert(details.name === "MongoDbHealthIndicator-ticket-registry");
            assert(details.proxyGrantingTicketsCache !== null);
            assert(details.ticketGrantingTicketsCache !== null);
            assert(details.proxyTicketsCache !== null);
            assert(details.serviceTicketsCache !== null);
            assert(details.transientSessionTicketsCache !== null);

            details = res.data.components.mongo.details["MongoDbHealthIndicator-service-registry"];
            assert(details.name === "MongoDbHealthIndicator-service-registry");

        }, async error => {
            throw error;
        }, { 'Content-Type': "application/json" });


    await cas.logg("Querying registry for all ticket-granting tickets");
    await cas.doGet(`${baseUrl}/ticketRegistry/query?prefix=TGT&count=10`, async res => {
        assert(res.status === 200);
        assert(res.data.length === 1);
    }, async err => {
        throw err;
    }, {
        'Accept': 'application/json',
        'Content-Type': "application/x-www-form-urlencoded"
    });

    // await cas.logg("Querying registry for all decoded ticket-granting tickets");
    // await cas.doGet(`${baseUrl}/ticketRegistry/query?prefix=TGT&decode=true`, async res => {
    //     assert(res.status === 200);
    //     assert(res.data.length === 1);
    // }, async err => {
    //     throw err;
    // }, {
    //     'Accept': 'application/json',
    //     'Content-Type': "application/x-www-form-urlencoded"
    // });

})();
