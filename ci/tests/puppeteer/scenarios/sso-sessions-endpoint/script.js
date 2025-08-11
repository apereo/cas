
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions";
    
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete(`${baseUrl}?type=ALL&from=1&count=1000`);

    await login();
    
    await cas.logg("Checking for SSO sessions for all users");
    await cas.doGet(`${baseUrl}?type=ALL`, (res) => {
        assert(res.status === 200);
        assert(Object.keys(res.data.activeSsoSessions).length === 4);
    }, (err) => {
        throw err;
    });

    await cas.logg("Checking for SSO sessions for a single user");
    await cas.doGet(`${baseUrl}?type=ALL&username=casuser3`, (res) => {
        assert(res.status === 200);
        assert(Object.keys(res.data.activeSsoSessions).length === 1);
    }, (err) => {
        throw err;
    });

    await cas.logg("Checking for SSO sessions via paging filters");
    await cas.doGet(`${baseUrl}?type=ALL&from=1&count=2`, (res) => assert(res.status === 200), (err) => {
        throw err;
    });

    await cas.logg("Checking for SSO sessions for single user");
    await cas.doGet(`${baseUrl}/users/casuser3`, (res) => {
        assert(res.status === 200);
        assert(Object.keys(res.data.activeSsoSessions).length === 1);
    }, (err) => {
        throw err;
    });

    await cas.logg("Removing all SSO Sessions for single user");
    await cas.doDelete(`${baseUrl}?type=ALL&usernam=casuser1`);
    await cas.doDelete(`${baseUrl}?type=ALL&usernam=casuser2`);
    await cas.doDelete(`${baseUrl}?type=ALL&usernam=casuser3`);
    await cas.doDelete(`${baseUrl}/users/casuser3`);

})();

async function login() {
    for (let i = 1; i <= 4; i++) {
        const browser = await cas.newBrowser(cas.browserOptions());
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await cas.gotoLogin(page);
        await cas.loginWith(page, `casuser${i}`, "Mellon");
        await cas.assertCookie(page);
        await context.close();
        await cas.closeBrowser(browser);
    }
}

