
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
    const service = "https://localhost:9859/anything/cas";
    try {
        mockServer = await cas.mockJsonServer(payload, 5423);
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.attributes.group !== undefined);
        assert(authenticationSuccess.attributes["email-address"] !== undefined);
    } finally {
        if (mockServer !== null) {
            mockServer.stop();
        }
        await cas.closeBrowser(browser);
    }
})();
