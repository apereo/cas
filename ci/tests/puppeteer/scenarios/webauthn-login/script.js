const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-webauthn");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(1000)
    
    let element = await page.$('#status');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Login with FIDO2-enabled Device")

    let errorPanel = await page.$('#errorPanel');
    assert(await errorPanel == null);

    let messages = await page.$('#messages');
    assert(await messages.boundingBox() != null);
    let deviceTable = await page.$('#deviceTable');
    assert(await deviceTable.boundingBox() == null);
    let authnButton = await page.$('#authnButton');
    assert(await authnButton.boundingBox() != null);

    const endpoints = ["health", "webAuthnDevices/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        const response = await page.goto(url);
        console.log(response.status() + " " + response.statusText())
        assert(response.ok())
    }

    await browser.close();
})();
