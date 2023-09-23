const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let properties = {
        callbackUrl: "https://localhost:8443/cas/login",
        properties: {
            "cas.loginUrl": "https://casserver.herokuapp.com/cas/login",
            "cas.protocol": "CAS20"
        }
    };
    let payload = {
        "/delegatedauthn": {
            "get": properties
        }
    };
    let mockServer = await cas.mockJsonServer(payload, 5432);
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, '#loginProviders');
    await cas.assertVisibility(page, 'li #CasClient');
    mockServer.stop();
    await browser.close();
})();
