
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
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    let json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.lastName[0] === "Johnson");
    assert(json.employeeNumber[0] === "123456");
    const originalFirstName = json.firstName[0];
    assert(originalFirstName !== undefined);
    assert(json.displayName === undefined);
    assert(json.cn === undefined);
    await cas.gotoLogout(page);

    service = "https://localhost:9859/anything/2";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    ticket = await cas.assertTicketParameter(page);
    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.firstName === undefined);
    assert(json.lastName === undefined);
    assert(json.employeeNumber === undefined);
    assert(json.displayName[0] === "Apereo");
    assert(json.cn[0] === "cas");
    
    await browser.close();
})();
