
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
    let payload = await cas.validateTicket(service, ticket);
    const authenticationSuccess = payload.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");

    await cas.log(`Validating ticket ${ticket} again on original node`);
    payload = await cas.validateTicket(service, ticket);
    const authenticationFailure = payload.serviceResponse.authenticationFailure;
    assert(authenticationFailure.code === "INVALID_TICKET");

    await logoutEverywhere(page);
}

async function ensureSessionsRecorded(page, port, conditions) {
    await cas.log(`Checking for recorded session via CAS server running on ${port}`);
    const url = `https://localhost:${port}/cas/actuator/ssoSessions?type=ALL`;
    await cas.log(`Navigating to ${url}`);
    await page.goto(url);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    console.dir(payload, {depth: null, colors: true});

    assert(payload.totalSsoSessions === 1);

    for (const ticket in conditions) {
        await cas.log(`Checking for issued ticket ${ticket}`);
        const service = conditions[ticket];
        assert(payload.activeSsoSessions[0].authenticated_services[ticket].id === service);
    }
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

(async () => {
    let failed = false;
    try {
        const browser = await cas.newBrowser(cas.browserOptions());
        await checkSessionsAreSynced(browser);
        await testBasicLoginLogout(browser);
        await checkTicketValidationAcrossNodes(browser);
        await browser.close();
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        if (!failed) {
            await process.exit(0);
        }
    }
})();
