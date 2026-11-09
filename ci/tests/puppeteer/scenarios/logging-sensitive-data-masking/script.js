const assert = require("assert");
const cas = require("../../cas.js");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SERVICE = "https://localhost:9859/anything/masking";

/**
 * DefaultCentralAuthenticationService logs every granted service ticket at INFO. The sanitizer replaces the ticket id
 * after its "ST-<n>-" prefix with sixteen asterisks and keeps only the tail (seven characters and the host name).
 */
const MASKED_SERVICE_TICKET = /Granted service ticket \[ST-\d+-\*{16}/;

async function readLogFile() {
    return cas.doRequest(`${ACTUATOR}/logfile`, "GET", {}, 200);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page);
        await cas.sleep(1000);
        const serviceTicket = await cas.assertTicketParameter(page);

        const sessions = JSON.parse(await cas.doRequest(`${ACTUATOR}/ssoSessions?type=ALL`, "GET",
            {"Accept": "application/json"}, 200)).activeSsoSessions;
        const ticketGrantingTicket = sessions
            .find((session) => Object.keys(session.authenticated_services).includes(serviceTicket))?.ticket_granting_ticket;
        assert(ticketGrantingTicket !== undefined, `No SSO session tracks ${serviceTicket}`);

        const validation = await cas.validateTicket(SERVICE, serviceTicket);
        assert.equal(validation.serviceResponse.authenticationSuccess.user, "casuser");
        const replay = await cas.validateTicket(SERVICE, serviceTicket);
        assert.equal(replay.serviceResponse.authenticationFailure.code, "INVALID_TICKET");

        let content = "";
        for (let attempt = 1; attempt <= 10 && !MASKED_SERVICE_TICKET.test(content); attempt++) {
            await cas.sleep(1000);
            content = await readLogFile();
        }
        await cas.log(`Read ${content.length} characters from the logfile endpoint`);
        const masked = content.split("\n").filter((line) => MASKED_SERVICE_TICKET.test(line));
        await cas.log(masked);
        assert(masked.length > 0, "The granted service ticket must be logged in its masked form");

        assert(!content.includes(serviceTicket), "The service ticket id must never be logged in full");
        assert(!content.includes(ticketGrantingTicket), "The ticket-granting ticket id must never be logged in full");
        assert(!content.includes("Mellon"), "The password must never be logged");

        await cas.gotoLogout(page);
        await context.close();
    } finally {
        await cas.closeBrowser(browser);
    }
})();
