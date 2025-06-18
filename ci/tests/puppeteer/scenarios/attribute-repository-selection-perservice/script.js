
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    let service = "https://localhost:9859/anything/1";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    let ticket = await cas.assertTicketParameter(page);
    let json = await cas.validateTicket(service, ticket);
    let attributes = json.serviceResponse.authenticationSuccess.attributes;
    assert(attributes.lastName[0] === "Johnson");
    assert(attributes.employeeNumber[0] === "123456");
    const originalFirstName = attributes.firstName[0];
    assert(originalFirstName !== undefined);
    assert(attributes.displayName === undefined);
    assert(attributes.cn === undefined);
    await cas.gotoLogout(page);

    service = "https://localhost:9859/anything/2";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    ticket = await cas.assertTicketParameter(page);
    json = await cas.validateTicket(service, ticket);
    attributes = json.serviceResponse.authenticationSuccess.attributes;
    assert(attributes.firstName === undefined);
    assert(attributes.lastName === undefined);
    assert(attributes.employeeNumber === undefined);
    assert(attributes.displayName[0] === "Apereo");
    assert(attributes.cn[0] === "cas");
    
    await browser.close();
})();
