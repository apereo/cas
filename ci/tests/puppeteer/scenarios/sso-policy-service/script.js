
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/sample&renew=true");
    await cas.loginWith(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page, false);
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas&renew=true");
    await cas.loginWith(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.sleep(2000);
    await cas.assertCookie(page);

    await verifyParticipationPolicies(page);
    await cas.closeBrowser(browser);
})();

/**
 * Asks for a ticket with the existing single sign-on session and checks whether the service took part in it.
 *
 * @param page the page
 * @param service the service URL
 * @param participates whether the session must be reused for the service
 */
async function verifySingleSignOn(page, service, participates) {
    await cas.log(`Requesting a ticket for ${service}; single sign-on must ${participates ? "" : "not "}apply`);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    if (participates) {
        await cas.assertTicketParameter(page);
    } else {
        await cas.assertTicketParameter(page, false);
        await cas.assertVisibility(page, "#username");
    }
}

/**
 * Services that never take part in single sign-on, that take part only while the session was used recently, and that
 * take part only for a while after the user logged in, however often the session is used.
 *
 * @param page the page, with a single sign-on session just established
 */
async function verifyParticipationPolicies(page) {
    await cas.logg("A service that never takes part in single sign-on asks for credentials");
    await verifySingleSignOn(page, "https://localhost:9859/anything/never", false);

    await cas.logg("Last used time and authentication date policies allow single sign-on right after login");
    await verifySingleSignOn(page, "https://localhost:9859/anything/authdate", true);
    await verifySingleSignOn(page, "https://localhost:9859/anything/lastused", true);

    await cas.logg("A session that was not used for a while is not reused for the last used time policy");
    await cas.sleep(5000);
    await verifySingleSignOn(page, "https://localhost:9859/anything/lastused", false);
    await verifySingleSignOn(page, "https://localhost:9859/anything/cas", true);
    await verifySingleSignOn(page, "https://localhost:9859/anything/authdate", true);

    await cas.logg("Using the session does not extend the authentication date policy");
    await cas.sleep(8000);
    await verifySingleSignOn(page, "https://localhost:9859/anything/cas", true);
    await verifySingleSignOn(page, "https://localhost:9859/anything/authdate", false);
}
