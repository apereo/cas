
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await startWithCasSp(page);
    await browser.close();
})();

async function startWithCasSp(page) {
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.gotoLogin(page, service);
    await cas.assertVisibility(page, "#selectProviderButton");
    await cas.submitForm(page, "#providerDiscoveryForm");
    await cas.sleep(3000);
    await cas.type(page, "#username", "casuser");
    
    await cas.submitForm(page, "#discoverySelectionForm");
    await cas.sleep(3000);
    await cas.loginWith(page);
    await cas.sleep(3000);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.validateTicket(service, ticket, "XML");
    assert(body.includes("<cas:user>casuser</cas:user>"));
}
