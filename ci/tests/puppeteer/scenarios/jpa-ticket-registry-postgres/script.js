const assert = require("assert");
const fs = require("fs");
const {JSONPath} = require("jsonpath-plus");
const cas = require("../../cas.js");

const startPuppeteerLoadTest = require("puppeteer-loadtest");
const args = process.argv.slice(2);
const config = JSON.parse(fs.readFileSync(args[0]));

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SERVICE = "https://localhost:9859/anything/cleaner";

async function runLoadTest() {
    const results = await startPuppeteerLoadTest({
        file: config.loadScript,
        samplesRequested: config.samplesRequested,
        concurrencyRequested: config.concurrencyRequested
    });
    cas.log(JSON.stringify(results, null, 2));
    const samples = JSONPath({path: "$..sample", json: results});
    assert(samples.length === parseInt(config.samplesRequested));
}

async function psql(sql) {
    const output = await cas.dockerExec("postgres-server", ["psql", "-U", "postgres", "-d", "tickets", "-t", "-A", "-c", sql]);
    return output.split("\n").map((line) => line.trim()).filter((line) => line.length > 0);
}

/**
 * The JPA ticket table, looked up by name rather than by entity name, since the physical naming strategy decides it.
 */
async function ticketTable() {
    const tables = await psql("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name LIKE '%tickets'");
    assert.equal(tables.length, 1, `Expected exactly one ticket table, found ${tables}`);
    return tables[0];
}

async function queryTicketRegistry(type) {
    return cas.doRequest(`${ACTUATOR}/ticketRegistry/query?type=${type}&count=1000`, "GET", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    }, 200);
}

async function activeSsoSessions() {
    const payload = JSON.parse(await cas.doRequest(`${ACTUATOR}/ssoSessions?type=ALL`, "GET",
        {"Accept": "application/json"}, 200));
    return payload.activeSsoSessions;
}

/**
 * Two sessions, one of them remember-me with a one-hour lifetime and an unused service ticket, the other with the
 * twenty-second primary policy. Nothing touches them afterwards, so only the scheduled cleaner can remove the
 * expired ticket-granting ticket and service ticket from the table, and it must leave the remember-me session alone.
 */
async function verifyTicketRegistryCleaner() {
    await cas.doDelete(`${ACTUATOR}/ssoSessions?type=ALL&from=0&count=100000`);
    const table = await ticketTable();

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const rememberedContext = await browser.createBrowserContext();
        const rememberedPage = await cas.newPage(rememberedContext);
        await cas.gotoLogin(rememberedPage, SERVICE);
        await cas.click(rememberedPage, "#rememberMeButton");
        await cas.loginWith(rememberedPage);
        await cas.sleep(1000);
        const serviceTicket = await cas.assertTicketParameter(rememberedPage);

        const expiringContext = await browser.createBrowserContext();
        const expiringPage = await cas.newPage(expiringContext);
        await cas.gotoLogin(expiringPage);
        await cas.loginWith(expiringPage);
        await cas.assertCookie(expiringPage);

        const sessions = await activeSsoSessions();
        assert.equal(sessions.length, 2, `Expected two SSO sessions, found ${JSON.stringify(sessions)}`);
        const expiring = sessions.find((session) => session.remember_me === false)?.ticket_granting_ticket;
        const remembered = sessions.find((session) => session.remember_me === true)?.ticket_granting_ticket;
        assert(expiring !== undefined && remembered !== undefined);
        await cas.log(`Expiring session ${expiring}, remember-me session ${remembered}, service ticket ${serviceTicket}`);

        let stored = await psql(`SELECT id FROM ${table}`);
        assert(stored.includes(expiring) && stored.includes(remembered) && stored.includes(serviceTicket),
            "All three tickets must be in the ticket table");
        let tickets = await queryTicketRegistry("TGT");
        assert(tickets.includes(expiring) && tickets.includes(remembered));
        assert((await queryTicketRegistry("ST")).includes(serviceTicket));

        await cas.logg("Waiting for the scheduled cleaner to remove the expired tickets");
        for (let attempt = 1; attempt <= 25; attempt++) {
            await cas.sleep(3000);
            stored = await psql(`SELECT id FROM ${table}`);
            if (!stored.includes(expiring) && !stored.includes(serviceTicket)) {
                await cas.log(`Expired tickets removed after ${attempt} check(s)`);
                break;
            }
        }
        assert(!stored.includes(expiring), "The expired ticket-granting ticket must be removed from the table");
        assert(!stored.includes(serviceTicket), "The expired service ticket must be removed from the table");
        assert(stored.includes(remembered), "The remember-me ticket-granting ticket is still valid and must stay");

        tickets = await queryTicketRegistry("TGT");
        assert(!tickets.includes(expiring) && tickets.includes(remembered));
        assert(!(await queryTicketRegistry("ST")).includes(serviceTicket));
        const remaining = (await activeSsoSessions()).map((session) => session.ticket_granting_ticket);
        assert(remaining.includes(remembered) && !remaining.includes(expiring));

        await cas.logg("The browser of the removed session must log in again");
        await cas.gotoLogin(expiringPage);
        await cas.assertVisibility(expiringPage, "#username");
        await cas.loginWith(expiringPage);
        await cas.assertCookie(expiringPage);
        await cas.assertInnerText(expiringPage, "#content div h2", "Log In Successful");

        await rememberedContext.close();
        await expiringContext.close();
    } finally {
        await cas.doDelete(`${ACTUATOR}/ssoSessions?type=ALL&from=0&count=100000`);
        await cas.closeBrowser(browser);
    }
}

(async () => {
    await runLoadTest();
    await verifyTicketRegistryCleaner();
})();
