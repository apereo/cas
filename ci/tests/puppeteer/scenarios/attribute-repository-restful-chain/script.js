const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const fs = require("fs");
const path = require("path");

(async () => {
    const attributes = {
        "mail": "casuser@example.org",
        "category": "USER",
    };
    let payload = {
        "/": {
            "get": attributes
        }
    };
    
    let mockServer = null;
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        mockServer = await cas.mockJsonServer(payload, 5423);
        const page = await cas.newPage(browser);
        const service = "https://apereo.github.io";
        await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);

        await cas.loginWith(page);
        let ticket = await cas.assertTicketParameter(page);
        let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.logg(body);
        let json = JSON.parse(body.toString());
        let authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.attributes.group !== undefined);
        assert(authenticationSuccess.attributes["email-address"] !== undefined);
    } finally {
        if (mockServer != null) {
            mockServer.stop();
        }
        await browser.close();
    }
})();
