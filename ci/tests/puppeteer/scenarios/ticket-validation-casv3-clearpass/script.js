
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);

    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType !== undefined);
    assert(authenticationSuccess.attributes.credential !== undefined);

    await browser.close();
})();
