const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.sleep(1000);
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        (res) => {
            assert(res.data.components.redis !== undefined);
            assert(res.data.components.memory !== undefined);
            assert(res.data.components.ping !== undefined);

            assert(res.data.components.redis.status !== undefined);
            assert(res.data.components.redis.details !== undefined);
        }, (error) => {
            throw error;
        }, {"Content-Type": "application/json"});
    await cas.closeBrowser(browser);

    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete(`${baseUrl}/ssoSessions?type=ALL&from=1&count=100000`);

    const formData = {
        username: "casuser",
        password: "Mellon"
    };
    const postData = querystring.stringify(formData);
    const total = 20;
    for (let i = 0; i < total; i++) {
        await cas.doRequest("https://localhost:8443/cas/v1/tickets", "POST",
            {
                "Accept": "application/json",
                "Content-Length": Buffer.byteLength(postData),
                "Content-Type": "application/x-www-form-urlencoded"
            },
            201, postData);
    }

    await cas.logg("Checking for SSO sessions for all users");
    await cas.doGet(`${baseUrl}/ssoSessions?type=ALL`, async (res) => assert(res.status === 200), (err) => {
        throw err;
    });

    await cas.logg("Querying registry for all ticket-granting tickets");
    await cas.doGet(`${baseUrl}/ticketRegistry/query?type=TGT&count=${total}`, async (res) => {
        assert(res.status === 200);
        assert(res.data.length === total);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });
    const ticketIds = [];
    await cas.logg("Querying registry for all decoded ticket-granting tickets");
    await cas.doGet(`${baseUrl}/ticketRegistry/query?type=TGT&count=${total}&decode=true`, async (res) => {
        await cas.log(res.data);
        assert(res.status === 200);
        assert(res.data.length === total);
        res.data.forEach((doc) => ticketIds.push(doc.id));
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });

    await cas.log(`Found ticket IDs: ${ticketIds.length}`);
    for (let i = 0; i < ticketIds.length; i++) {
        const id = ticketIds[i];
        await cas.log(`Querying ticket: ${id}`);
        await cas.doGet(`${baseUrl}/ticketRegistry/query?type=TGT&id=${id}&decode=false`,
            async (res) => assert(res.status === 200),
            async (err) => {
                throw err;
            }, {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded"
            });
    }
})();

