const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete(`${baseUrl}/ssoSessions?type=ALL&from=1&count=100000`);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.sleep(1000);
    await cas.closeBrowser(browser);

    await cas.doGet("https://localhost:8443/cas/actuator/health",
        async (res) => {
            assert(res.data.components.mongo !== undefined);
            assert(res.data.components.memory !== undefined);
            assert(res.data.components.ping !== undefined);

            assert(res.data.components.mongo.status !== undefined);
            assert(res.data.components.mongo.details !== undefined);

            let details = res.data.components.mongo.details["MongoDbHealthIndicator-ticket-registry"];
            assert(details.name === "MongoDbHealthIndicator-ticket-registry");
            assert(details.proxyGrantingTicketsCollection !== undefined);
            assert(details.ticketGrantingTicketsCollection !== undefined);
            assert(details.proxyTicketsCollection !== undefined);
            assert(details.serviceTicketsCollection !== undefined);
            assert(details.transientSessionTicketsCollection !== undefined);

            details = res.data.components.mongo.details["MongoDbHealthIndicator-service-registry"];
            assert(details.name === "MongoDbHealthIndicator-service-registry");

        }, async (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.logg("Querying registry for ticket-granting tickets");
    await cas.doGet(`${baseUrl}/ticketRegistry/query?type=TGT&count=10`, async (res) => {
        assert(res.status === 200);
        assert(res.data.length === 1);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });

    await cas.doDelete(`${baseUrl}/ticketRegistry/clean`, 200,
        async (res) => {
            await cas.log(res.data);
            assert(res.status === 200);
            assert(res.data.removed === 0);
            assert(res.data.startTime !== undefined);
            assert(res.data.endTime !== undefined);
            assert(res.data.total !== undefined);
        }, async (err) => {
            throw err;
        }, {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        });
})();
