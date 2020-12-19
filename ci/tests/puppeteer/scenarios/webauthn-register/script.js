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
    assert(header === "Register Device")

    var messages = await page.$('#messages');
    assert(await messages.boundingBox() != null);
    
    var dInfo = await page.$('#device-info');
    assert(await dInfo.boundingBox() == null);
    var dIcon = await page.$('#device-icon');
    assert(await dIcon.boundingBox() == null);
    var dName = await page.$('#device-name');
    assert(await dName.boundingBox() == null);

    var credentialNickname = await page.$('#credentialNickname');
    assert(await credentialNickname.boundingBox() != null);
    var registerButton = await page.$('#registerButton');
    assert(await registerButton.boundingBox() != null);
    
    await browser.close();
})();
