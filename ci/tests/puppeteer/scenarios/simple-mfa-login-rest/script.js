
const cas = require("../../cas.js");

(async () => {
    const principal = {
        "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
        "id": "casuser"
    };

    const payload = {
        "/new": {
            "post": "514a61cc1518"
        },
        "/": {
            "post": "514a61cc1518"
        },
        "/:id": {
            "post": principal,
            "get": principal
        }
    };

    let mockServer = null;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {

        mockServer = await cas.mockJsonServer(payload, 5432);
        const page = await cas.newPage(browser);

        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.assertVisibility(page, "#token");

        const code = await cas.extractFromEmail(browser);

        await page.bringToFront();
        await cas.type(page, "#token", code);
        await cas.submitForm(page, "#fm1");
        await cas.sleep(1000);
        await cas.submitForm(page, "#registerform");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
    } finally {
        if (mockServer !== null) {
            mockServer.stop();
        }
        await cas.closeBrowser(browser);
    }
})();
