
const cas = require("../../cas.js");
const assert = require("assert");

async function returnCasResponse(page, appId) {
    await cas.log(`Producing CAS response for appId ${appId}`);
    await cas.gotoLogout(page);
    const service = `http://localhost:9889/anything/${appId}`;
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    const response = await cas.validateTicket(service, ticket);
    return response.serviceResponse.authenticationSuccess;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let response = await returnCasResponse(page, "app1");
    assert(response.user === "casuser");
    assert(response.attributes.firstname[0] === "CAS");
    assert(response.attributes.lastname[0] === "User");
    assert(response.attributes.username[0] === "casuser");
    let count = Object.keys(response.attributes).length;
    assert(count === 3);
    
    response = await returnCasResponse(page, "app2");
    assert(response.user === "casuser");
    assert(response.attributes.color[0] === "Yellow");
    assert(response.attributes.department[0] === "IAM");
    assert(response.attributes.username[0] === "casuser");
    count = Object.keys(response.attributes).length;
    assert(count === 3);

    response = await returnCasResponse(page, "app3");
    assert(response.user === "casuser");
    assert(response.attributes.nickname[0] === "Bob");
    assert(response.attributes.employeeNumber[0] === "123456");
    count = Object.keys(response.attributes).length;
    assert(count === 2);

    await browser.close();
})();
