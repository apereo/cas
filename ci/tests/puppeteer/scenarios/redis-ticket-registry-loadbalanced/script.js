const assert = require("assert");
const cas = require("../../cas.js");

async function ensureNoSsoSessionsExistAfterLogout(page, port) {
    const url = `https://localhost:${port}/cas/actuator/ssoSessions?type=ALL`;
    await cas.log(`Navigating to ${url}`);
    await page.goto(url);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.totalSsoSessions === 0);
}

async function testBasicLoginLogout(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(8000);
    await cas.screenshot(page);
    const ticket = await cas.assertTicketParameter(page);
    const payload = await cas.validateTicket(service, ticket);
    const authenticationSuccess = payload.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    await logoutEverywhere(page);
    await ensureNoSsoSessionsExistAfterLogout(page, 8443);
    await ensureNoSsoSessionsExistAfterLogout(page, 8444);
}

async function logoutEverywhere(page) {
    await cas.gotoLogout(page);
    await cas.goto(page, "https://localhost:8444/cas/logout");
}

async function checkTicketValidationAcrossNodes(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);

    const service = "https://localhost:9859/anything/100";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    const ticket = await cas.assertTicketParameter(page);

    await cas.log("Validating ticket on second node");
    let payload = await cas.validateTicket(service, ticket, "JSON", 8444);
    const authenticationSuccess = payload.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");

    await cas.log(`Validating ticket ${ticket} again on original node`);
    payload = await cas.validateTicket(service, ticket, "JSON", 8443);
    const authenticationFailure = payload.serviceResponse.authenticationFailure;
    assert(authenticationFailure.code === "INVALID_TICKET");

    await logoutEverywhere(page);
}

async function ensureSessionsRecorded(page, port, conditions) {
    await cas.log(`Checking for recorded session via CAS server running on ${port}`);
    const url = `https://localhost:${port}/cas/actuator/ssoSessions?type=ALL`;
    await cas.doGet(url, async (res) => {
        const payload = res.data;
        assert(payload.totalSsoSessions === 1);
        for (const ticket in conditions) {
            await cas.log(`Checking for issued ticket ${ticket}`);
            const service = conditions[ticket];
            assert(payload.activeSsoSessions[0].authenticated_services[ticket].id === service);
        }
    }, (error) => {
        throw error;
    }, {
        "Content-Type": "application/json"
    });
}

async function checkSessionsAreSynced(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);

    const s1 = "https://localhost:9859/anything/1";
    const s2 = "https://localhost:9859/anything/2";
    const s3 = "https://localhost:9859/anything/3";

    await cas.log("Getting first ticket");
    await cas.gotoLogin(page, s1);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    const ticket1 = await cas.assertTicketParameter(page);

    await cas.log("Getting second ticket");
    await cas.gotoLogin(page, s2);
    await cas.sleep(3000);
    const ticket2 = await cas.assertTicketParameter(page);

    await cas.log("Getting third ticket");
    await cas.gotoLogin(page, s3);
    await cas.sleep(3000);
    const ticket3 = await cas.assertTicketParameter(page);

    const conditions = {
        [ticket1]: s1,
        [ticket2]: s2,
        [ticket3]: s3
    };
    await cas.sleep(3000);
    await ensureSessionsRecorded(page, 8443, conditions);
    await ensureSessionsRecorded(page, 8444, conditions);

    await logoutEverywhere(page);
}

/**
 * The redisTicketsCache endpoint exposes each node's own first-level cache: a ticket added on one node is cached
 * there and not on the other, evicting it leaves Redis alone, and a ticket removed through the other node is evicted
 * from this node's cache by the registry's messages.
 */
async function checkRedisTicketsCache(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);
    const service = "https://localhost:9859/anything/cache";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(3000);
    const ticket = await cas.assertTicketParameter(page);
    const cacheUrl = (port, id) => `https://localhost:${port}/cas/actuator/redisTicketsCache/${id}`;
    const json = {"Accept": "application/json"};

    const cached = await cas.doRequest(cacheUrl(8443, ticket), "GET", json, 200);
    assert(cached.includes(ticket));
    await cas.doRequest(cacheUrl(8444, ticket), "GET", json, 404);

    const sessions = JSON.parse(await cas.doRequest("https://localhost:8443/cas/actuator/ssoSessions?type=ALL", "GET", json, 200));
    const session = sessions.activeSsoSessions.find((entry) => entry.authenticated_services[ticket] !== undefined);
    assert(session !== undefined);
    const tgt = session.ticket_granting_ticket;
    const evicted = await cas.doRequest(cacheUrl(8443, tgt), "DELETE", json, 200);
    assert(evicted.includes(tgt));
    await cas.doRequest(cacheUrl(8443, tgt), "GET", json, 404);
    await cas.doRequest(cacheUrl(8443, tgt), "DELETE", json, 404);
    await cas.gotoLogin(page, undefined, 8443);
    await cas.assertCookie(page);

    const payload = await cas.validateTicket(service, ticket, "JSON", 8444);
    assert(payload.serviceResponse.authenticationSuccess.user === "casuser");
    await cas.sleep(2000);
    await cas.doRequest(cacheUrl(8443, ticket), "GET", json, 404);
    await logoutEverywhere(page);
}

async function checkSsoSessionsWithActuator(browser) {
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete("https://localhost:8443/cas/actuator/ssoSessions?type=ALL&from=1&count=1000");
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);

    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(3000);
    const ticket1 = await cas.assertTicketParameter(page);
    const conditions = {
        [ticket1]: service
    };
    for (const port of [8443, 8444]) {
        await ensureSessionsRecorded(page, port, conditions);
    }
    for (const port of [8443, 8444]) {
        await cas.gotoLogin(page, undefined, port);
        await cas.assertCookie(page);
    }
    await cas.log("Removing SSO sessions from original node...");
    await cas.doDelete("https://localhost:8443/cas/actuator/ssoSessions?username=casuser");

    for (const port of [8443, 8444]) {
        await cas.gotoLogin(page, undefined, port);
        await cas.assertCookie(page, false);
    }

    for (const port of [8443, 8444]) {
        const url = `https://localhost:${port}/cas/actuator/ssoSessions?username=casuser`;
        await cas.doGet(url, async (res) => {
            const payload = res.data;
            assert(payload.totalSsoSessions === 0);
        }, (error) => {
            throw error;
        }, {
            "Content-Type": "application/json"
        });
    }
}

(async () => {
    let failed = false;
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    try {
        await checkSessionsAreSynced(browser);
        await testBasicLoginLogout(browser);
        await checkTicketValidationAcrossNodes(browser);
        await checkRedisTicketsCache(browser);
        await checkSsoSessionsWithActuator(browser);
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await context.close();
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
