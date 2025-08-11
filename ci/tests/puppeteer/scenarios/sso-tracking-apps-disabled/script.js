
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions";

    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete(`${baseUrl}?type=ALL&from=1&count=1000`);

    await login("https://localhost:9859/anything/cas");

    await cas.logg("Checking for SSO sessions for all users");
    await cas.doGet(`${baseUrl}?type=ALL`, async (res) => {
        assert(res.status === 200);
        const index = Object.keys(res.data.activeSsoSessions).length - 1;
        const activeSession = res.data.activeSsoSessions[index];
        await cas.log(JSON.stringify(activeSession.authenticated_services));
        assert(activeSession.number_of_uses === 4);
        const services = activeSession.authenticated_services;
        assert(Object.keys(services).length === 4);
    }, async (err) => {
        throw err;
    });
})();

async function login(service) {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    for (let i = 1; i <= 4; i++) {
        await cas.log(`Logging into CAS; attempt ${i}`);
        await cas.gotoLogin(page, service);
        if (i === 1) {
            await cas.loginWith(page, "casuser", "Mellon");
        }
        await cas.assertTicketParameter(page);
    }
    await cas.closeBrowser(browser);
}

