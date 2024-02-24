const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

async function ensureNoSsoSessionsExistAfterLogout(page, port) {
    const url = `https://localhost:${port}/cas/actuator/ssoSessions?type=ALL`;
    await cas.log(`Navigating to ${url}`);
    await page.goto(url);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.totalTicketGrantingTickets === 0);
    assert(payload.totalTickets === 0);
}

async function testBasicLoginLogout(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);
    const service = "https://apereo.github.io";
    await cas.gotoLogin(page, service);
    await cas.waitForTimeout(page);
    await cas.loginWith(page);
    await cas.waitForTimeout(page);
    const ticket = await cas.assertTicketParameter(page);
    await page.goto(`https://localhost:8444/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    const authenticationSuccess = payload.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    await logoutEverywhere(page);
    await ensureNoSsoSessionsExistAfterLogout(page, 8443);
    await ensureNoSsoSessionsExistAfterLogout(page, 8444);
}

async function logoutEverywhere(page) {
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.goto(page, "https://localhost:8444/cas/logout");
}

async function checkTicketValidationAcrossNodes(browser) {
    const page = await cas.newPage(browser);
    await logoutEverywhere(page);

    const service = "https://localhost:9859/anything/100";
    await cas.gotoLogin(page, service);
    await cas.waitForTimeout(page);
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);

    await cas.log("Validating ticket on second node");
    await page.goto(`https://localhost:8444/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    let content = await cas.textContent(page, "body");
    let payload = JSON.parse(content);
    const authenticationSuccess = payload.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");

    await cas.log(`Validating ticket ${ticket} again on original node`);
    await page.goto(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    content = await cas.textContent(page, "body");
    payload = JSON.parse(content);
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

    assert(payload.totalTicketGrantingTickets === 1);
    assert(payload.totalTickets === 1);

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
    const s2 = "https://apereo.github.io";
    const s3 = "https://example.org";

    await cas.log("Getting first ticket");
    await cas.gotoLogin(page, s1);
    await cas.waitForTimeout(page);
    await cas.loginWith(page);
    const ticket1 = await cas.assertTicketParameter(page);

    await cas.log("Getting second ticket");
    await cas.gotoLogin(page, s2);
    await cas.waitForTimeout(page);
    const ticket2 = await cas.assertTicketParameter(page);

    await cas.log("Getting third ticket");
    await cas.gotoLogin(page, s3);
    await cas.waitForTimeout(page);
    const ticket3 = await cas.assertTicketParameter(page);

    const conditions = {
        [ticket1]: s1,
        [ticket2]: s2,
        [ticket3]: s3
    };
    await cas.waitForTimeout(page);
    await ensureSessionsRecorded(page, 8443, conditions);
    await ensureSessionsRecorded(page, 8444, conditions);

    await logoutEverywhere(page);
}

(async () => {
    let failed = false;
    try {
        const browser = await puppeteer.launch(cas.browserOptions());
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
