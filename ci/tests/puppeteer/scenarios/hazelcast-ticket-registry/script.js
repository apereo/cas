const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

async function verifySsoSessions() {
    const formData = {
        username: "casuser",
        password: "Mellon"
    };
    const postData = querystring.stringify(formData);
    for (let i = 0; i < 10; i++) {
        const tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", "POST",
            201, "application/x-www-form-urlencoded", postData);
        assert(tgt !== undefined);
    }

    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions";
    await cas.doGet(`${baseUrl}/users/casuser`, (res) => {
        assert(res.status === 200);
        assert(Object.keys(res.data.activeSsoSessions).length > 1);
    }, (err) => {
        throw err;
    });
}

async function executeRequest(url, method, statusCode,
    contentType = "application/x-www-form-urlencoded", requestBody = undefined) {
    return cas.doRequest(url, method,
        {
            "Accept": "application/json",
            "Content-Length": requestBody === undefined ? 0 : Buffer.byteLength(requestBody),
            "Content-Type": contentType
        },
        statusCode, requestBody);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.sleep(1000);
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
    await cas.sleep();
    await cas.assertCookie(page);
    await cas.sleep();
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);

    await verifySsoSessions();

})();
