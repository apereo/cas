const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
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

    var errorPanel = await page.$('#errorPanel');
    assert(await errorPanel == null);

    var messages = await page.$('#messages');
    assert(await messages.boundingBox() != null);
    var deviceTable = await page.$('#deviceTable');
    assert(await deviceTable.boundingBox() == null);
    var authnButton = await page.$('#authnButton');
    assert(await authnButton.boundingBox() != null);

    await browser.close();
})();
