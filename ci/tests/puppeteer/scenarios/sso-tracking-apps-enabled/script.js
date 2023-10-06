const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions";

    await cas.logg("Removing all SSO Sessions");
    await cas.doRequest(`${baseUrl}?type=ALL&from=1&count=1000`, "DELETE", {});

    await login("https://apereo.github.io");

    await cas.logg("Checking for SSO sessions for all users");
    await cas.doGet(`${baseUrl}?type=ALL`, res => {
        assert(res.status === 200);
        let index = Object.keys(res.data.activeSsoSessions).length - 1;
        let activeSession = res.data.activeSsoSessions[index];
        cas.log(JSON.stringify(activeSession.authenticated_services));
        assert(activeSession.number_of_uses === 4);
        let services = activeSession.authenticated_services;
        assert(Object.keys(services).length) === 1;
    }, err => {
        throw err;
    })
})();

async function login(service) {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    for (let i = 1; i <= 4; i++) {
        await cas.log(`Logging into CAS; attempt ${i}`);
        await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
        if (i === 1) {
            await cas.loginWith(page, `casuser`, "Mellon");
        }
        await cas.assertTicketParameter(page);
    }
    await browser.close();
}

