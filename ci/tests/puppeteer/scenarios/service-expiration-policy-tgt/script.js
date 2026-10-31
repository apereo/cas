
const assert = require("assert");
const cas = require("../../cas.js");

const ST_SERVICE = "https://localhost:9859/anything/st";

/**
 * Validates a service ticket and reports whether validation succeeded.
 *
 * @param ticket the service ticket
 * @returns true if the ticket validated
 */
async function validates(ticket) {
    const json = await cas.validateTicket(ST_SERVICE, ticket);
    return json.serviceResponse.authenticationSuccess !== undefined;
}

/**
 * The service ticket policy of a service overrides the global one, which allows one use within ten seconds.
 *
 * @param page the page
 */
async function verifyServiceTicketPolicy(page) {
    await cas.gotoLogin(page, ST_SERVICE);
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    assert(await validates(ticket), "First use of the service ticket must validate");
    assert(await validates(ticket), "Second use of the service ticket must validate");
    assert(!await validates(ticket), "Third use of the service ticket must be refused");

    await cas.gotoLogin(page, ST_SERVICE);
    const expiring = await cas.assertTicketParameter(page);
    await cas.sleep(4000);
    assert(!await validates(expiring), "Service ticket must expire after its time to live");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(4000);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await verifyServiceTicketPolicy(page);
    await cas.closeBrowser(browser);
})();
