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

    let messages = await page.$('#messages');
    assert(await messages.boundingBox() != null);
    
    let dInfo = await page.$('#device-info');
    assert(await dInfo.boundingBox() == null);
    let dIcon = await page.$('#device-icon');
    assert(await dIcon.boundingBox() == null);
    let dName = await page.$('#device-name');
    assert(await dName.boundingBox() == null);

    let credentialNickname = await page.$('#credentialNickname');
    assert(await credentialNickname.boundingBox() != null);
    let registerButton = await page.$('#registerButton');
    assert(await registerButton.boundingBox() != null);
    
    let residentKeysPanel = await page.$('#residentKeysPanel');
    assert(await residentKeysPanel.boundingBox() != null);
    let registerResident = await page.$('#registerDiscoverableCredentialButton');
    assert(await registerResident.boundingBox() != null);

    await browser.close();
})();
