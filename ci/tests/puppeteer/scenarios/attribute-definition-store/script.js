
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        await cas.loginWith(page);
        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        const json = JSON.parse(body);
        console.dir(json, {depth: null, colors: true});
        const success = json.serviceResponse.authenticationSuccess;
        assert(success.attributes.mail !== undefined);
        assert(success.attributes["external-groups"] !== undefined);
    } finally {
        await browser.close();
    }
})();

