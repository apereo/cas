
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertCookie(page, false);
    await cas.assertTextContent(page, "#interruptMessage", "We interrupted your login");
    await cas.assertVisibility(page, "#interruptLinks");
    await cas.assertVisibility(page, "#attributesTable");
    await cas.assertVisibility(page, "#field1");
    await cas.assertVisibility(page, "#field1-value");
    await cas.assertVisibility(page, "#field2");
    await cas.assertVisibility(page, "#field2-value");

    await cas.assertInvisibility(page, "#cancel");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes("duobypass"));
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
