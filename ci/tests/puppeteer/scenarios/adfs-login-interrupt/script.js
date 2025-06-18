
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.click(page, "div .idp span");
    await cas.sleep(4000);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await cas.sleep(1000);
    await cas.submitForm(page, "#loginForm");
    await cas.sleep(3000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContentStartsWith(page, "#interruptMessage", "We interrupted your login");
    await cas.submitForm(page, "#fm1");

    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(3000);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes("casuser@apereo.org"));
    assert(authenticationSuccess.attributes.firstname !== undefined);
    assert(authenticationSuccess.attributes.lastname !== undefined);
    assert(authenticationSuccess.attributes.uid !== undefined);
    assert(authenticationSuccess.attributes.upn !== undefined);
    assert(authenticationSuccess.attributes.username !== undefined);
    assert(authenticationSuccess.attributes.surname !== undefined);
    assert(authenticationSuccess.attributes.email !== undefined);
    await browser.close();
})();
