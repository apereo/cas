const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const principal = {
        "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
        "id": "casuser"
    };

    let payload = {
        "/new": {
            "get": '514a61cc1518'
        },
        "/": {
            "post": '514a61cc1518'
        },
        "/:id": {
            "get": principal
        }
    };

    let mockServer = null;
    const browser = await puppeteer.launch(cas.browserOptions());
    try {

        mockServer = await cas.mockJsonServer(payload, 5432);
        const page = await cas.newPage(browser);

        await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-simple");
        await cas.loginWith(page);
        await page.waitForTimeout(1000);
        await cas.assertVisibility(page, '#token');

        const page2 = await browser.newPage();
        await page2.goto("http://localhost:8282");
        await page2.waitForTimeout(1000);
        await cas.click(page2, "table tbody td a");
        await page2.waitForTimeout(1000);
        let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
        await page2.close();

        await page.bringToFront();
        await cas.type(page, "#token", code);
        await cas.submitForm(page, "#fm1");
        await page.waitForTimeout(1000);
        await cas.submitForm(page, "#registerform");
        await page.waitForTimeout(1000);
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
        await cas.assertCookie(page);
    } finally {
        if (mockServer != null) {
            mockServer.stop();
        }
        await browser.close();
    }
})();
