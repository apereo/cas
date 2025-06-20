
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await executeRequest(page, "https://localhost:9859/anything/1", "groupMembership1", "STUD");
    await executeRequest(page, "https://localhost:9859/anything/2", "groupMembership2", "ADMN");
    await executeRequest(page, "https://localhost:9859/anything/3", "groupMembership3", "FACULTY");
    await executeRequest(page, "https://localhost:9859/anything/4", "groupMembership4", "COURSE-H101");
    await executeRequest(page, "https://localhost:9859/anything/5", "COURSE", "CHEMISTRY-101");
    await executeRequest(page, "https://localhost:9859/anything/6", "COURSE", "SOFTENG-101");
    await browser.close();
})();

async function executeRequest(page, service, attribute, attributeValue) {
    await cas.log(`Running tests for service ${service} with attribute requirements ${attribute}:${attributeValue}`);
    
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.accountId === undefined);
    assert(authenticationSuccess.attributes.credentialType === undefined);
    assert(authenticationSuccess.attributes.authenticationDate === undefined);
    assert(authenticationSuccess.attributes.isFromNewLogin === undefined);
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed === undefined);
    assert(authenticationSuccess.attributes[attribute][0] === attributeValue);
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.log("============================");
    
    return authenticationSuccess;
}
