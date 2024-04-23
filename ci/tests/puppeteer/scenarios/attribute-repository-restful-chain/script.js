
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const attributes = {
        "mail": "casuser@example.org",
        "category": "USER"
    };
    const payload = {
        "/": {
            "get": attributes
        }
    };
    
    let mockServer = null;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        mockServer = await cas.mockJsonServer(payload, 5423);
        const page = await cas.newPage(browser);
        const service = "https://apereo.github.io";
        await cas.gotoLogin(page, service);

        await cas.loginWith(page);
        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.logg(body);
        const json = JSON.parse(body.toString());
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.attributes.group !== undefined);
        assert(authenticationSuccess.attributes["email-address"] !== undefined);
    } finally {
        if (mockServer !== null) {
            mockServer.stop();
        }
        await browser.close();
    }
})();
