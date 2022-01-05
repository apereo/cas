const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions"
    
    await cas.logg("Removing all SSO Sessions")
    await cas.doRequest(`${baseUrl}?type=ALL&from=1&count=1000`, "DELETE", {});

    await login();
    
    await cas.logg("Checking for SSO sessions for all users")
    await cas.doGet(`${baseUrl}?type=ALL`, res => {
        assert(res.status === 200)
        assert(Object.keys(res.data.activeSsoSessions).length === 4)
    }, err => {
        throw err;
    })

    await cas.logg("Checking for SSO sessions for a single user")
    await cas.doGet(`${baseUrl}?type=ALL&username=casuser3`, res => {
        assert(res.status === 200)
        assert(Object.keys(res.data.activeSsoSessions).length === 1)
    }, err => {
        throw err;
    })

    await cas.logg("Checking for SSO sessions via paging filters")
    await cas.doGet(`${baseUrl}?type=ALL&from=1&count=2`, res => {
        assert(res.status === 200)
        assert(res.data.totalTicketGrantingTickets === 2)
        assert(res.data.totalPrincipals === 2)
        assert(res.data.totalTickets === 2)
    }, err => {
        throw err;
    })


    await cas.logg("Removing all SSO Sessions for single user")
    await cas.doRequest(`${baseUrl}?type=ALL&usernam=casuser1`, "DELETE", {});
    await cas.doRequest(`${baseUrl}?type=ALL&usernam=casuser2`, "DELETE", {});
    await cas.doRequest(`${baseUrl}?type=ALL&usernam=casuser3`, "DELETE", {});

})();

async function login() {
    for (let i = 1; i <= 4; i++) {
        const browser = await puppeteer.launch(cas.browserOptions({args: ['--incognito']}));
        const page = await cas.newPage(browser);
        await page.goto("https://localhost:8443/cas/login");
        await cas.loginWith(page, `casuser${i}`, "Mellon");
        await cas.assertTicketGrantingCookie(page);
        await browser.close();
    }
}

