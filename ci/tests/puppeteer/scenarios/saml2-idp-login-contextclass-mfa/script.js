const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        await cas.goto(page, "https://localhost:9876/sp");
        await page.waitForTimeout(3000);
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(2000);
        await page.waitForSelector('#username', {visible: true});
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(3000);
        console.log(`Page URL: ${page.url()}`);
        console.log("Fetching Scratch codes from /cas/actuator...");
        let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        console.log(`Using scratch code ${scratch} to login...`);
        await cas.screenshot(page);
        await cas.type(page,'#token', scratch);
        await page.keyboard.press('Enter');
        await page.waitForNavigation();
        console.log(`Page URL: ${page.url()}`);
        await page.waitForTimeout(3000);
        await cas.screenshot(page);
        await cas.assertInnerText(page, "#principal", "casuser");
        await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa");
        await browser.close();
        await cleanUp();
        console.log('Cleanup done');
    }, async error => {
        await cleanUp();
        console.log(error);
        throw error;
    })
})();

