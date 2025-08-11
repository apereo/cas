
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "castest", process.env.AZURE_AD_USER_PASSWORD);
    const ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    const json = await cas.validateTicket(service, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success.attributes.givenName[0] === "CAS");
    assert(success.attributes.displayName[0] === "CAS Test");
    assert(success.attributes.jobTitle[0] === "Tester");
    await cas.closeBrowser(browser);
})();
