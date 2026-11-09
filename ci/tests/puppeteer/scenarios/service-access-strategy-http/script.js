
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

    for (const period of ["timepast", "timefuture"]) {
        await cas.log(`A service whose access window is ${period} is refused and issues no ticket`);
        const response = await cas.gotoLogin(page, `https://localhost:9859/anything/${period}`);
        await cas.sleep(1000);
        assert.equal(response.status(), 403);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
        await cas.assertElementDoesNotExist(page, "#username");
    }

    await cas.log("A service whose access window includes the current time issues a ticket that validates");
    const service = "https://localhost:9859/anything/timenow";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
    }
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    assert.equal(json.serviceResponse.authenticationSuccess.user, "casuser");
    await cas.closeBrowser(browser);
})();
