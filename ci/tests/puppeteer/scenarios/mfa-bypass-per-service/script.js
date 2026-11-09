
const assert = require("assert");
const cas = require("../../cas.js");

/*
 * Each application's multifactor policy decides on its own whether YubiKey may be skipped. The client address is
 * the browser's loopback address; the stub attribute repository gives casuser memberOf=student and no affiliation.
 */

async function withPage(browser, callback) {
    const context = await browser.createBrowserContext();
    try {
        return await callback(await cas.newPage(context));
    } finally {
        await context.close();
    }
}

async function verifyBypassed(browser, service) {
    await withPage(browser, async (page) => {
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "casuser");
        assert(authenticationSuccess.attributes.bypassMultifactorAuthentication[0] === true);
        assert(authenticationSuccess.attributes.bypassedMultifactorAuthenticationProviderId[0] === "mfa-yubikey");
    });
}

async function verifyYubiKeyRequired(browser, service) {
    await withPage(browser, async (page) => {
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.assertTicketParameter(page, false);
        await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");
        await cas.assertVisibility(page, "#token");
    });
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://github.com");
    await cas.loginWith(page);

    await cas.sleep(1000);
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);

    await cas.logg("A request from an address the policy lists skips YubiKey");
    await verifyBypassed(browser, "https://localhost:9859/anything/bypass-local-address");
    await cas.logg("A request from any other address must use YubiKey");
    await verifyYubiKeyRequired(browser, "https://localhost:9859/anything/bypass-remote-address");

    await cas.logg("A user without the bypass attribute skips YubiKey when the policy allows it");
    await verifyBypassed(browser, "https://localhost:9859/anything/bypass-missing-attribute");
    await cas.logg("A user without the bypass attribute must use YubiKey when the policy does not allow it");
    await verifyYubiKeyRequired(browser, "https://localhost:9859/anything/require-missing-attribute");
    await cas.logg("A user whose attribute has another value must use YubiKey, even when missing attributes are bypassed");
    await verifyYubiKeyRequired(browser, "https://localhost:9859/anything/bypass-mismatched-attribute");

    await cas.closeBrowser(browser);
})();
