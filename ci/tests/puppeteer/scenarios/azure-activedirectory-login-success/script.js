
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
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    const json = JSON.parse(body);
    // console.dir(json, {depth: null, colors: true});
    const success = json.serviceResponse.authenticationSuccess;
    assert(success.attributes.givenName[0] === "CAS");
    assert(success.attributes.displayName[0] === "CAS Test");
    assert(success.attributes.jobTitle[0] === "Tester");
    await browser.close();
})();
