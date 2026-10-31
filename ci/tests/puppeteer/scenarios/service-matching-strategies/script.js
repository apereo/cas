const cas = require("../../cas.js");

const BASE = "https://localhost:9859/anything";

/**
 * Asks for a service ticket with an existing single sign-on session and checks whether the service is accepted.
 *
 * @param page the page
 * @param service the service URL
 * @param accepted whether a registered service must match the URL
 */
async function verifyService(page, service, accepted) {
    await cas.log(`Requesting a ticket for ${service}; it must be ${accepted ? "accepted" : "refused"}`);
    await cas.gotoLogin(page, encodeURIComponent(service));
    await cas.sleep(1000);
    if (accepted) {
        await cas.assertTicketParameter(page);
        await cas.assertPageUrlStartsWith(page, service);
    } else {
        await cas.assertTextContent(page, "#content h2", "Application Not Authorized to Use CAS");
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, encodeURIComponent(`${BASE}/literal`));
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.logg("Literal: only the exact service id matches");
    await verifyService(page, `${BASE}/literal`, true);
    await verifyService(page, `${BASE}/literal/extra`, false);
    await verifyService(page, `${BASE}/LITERAL`, false);

    await cas.logg("Starts with: the expected URL is a prefix, so a trailing slash keeps look-alike paths out");
    await verifyService(page, `${BASE}/startswith/app`, true);
    await verifyService(page, `${BASE}/startswithevil`, false);

    await cas.logg("Full regex: the whole URL must match the pattern");
    await verifyService(page, `${BASE}/fullregex/app`, true);
    await verifyService(page, `${BASE}/fullregex/app/extra`, false);
    await verifyService(page, `${BASE}/other?next=${BASE}/fullregex/app`, false);

    await cas.logg("Partial regex: the pattern may match anywhere in the URL");
    await verifyService(page, `${BASE}/partialregex/app`, true);
    await verifyService(page, `${BASE}/other/partialregex/app`, false);
    await verifyService(page, `${BASE}/other?next=${BASE}/partialregex/`, true);

    await cas.closeBrowser(browser);
})();
