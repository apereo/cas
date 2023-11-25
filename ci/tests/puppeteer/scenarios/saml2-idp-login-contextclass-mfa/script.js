const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        await cas.goto(page, "https://localhost:9876/sp");
        await page.waitForTimeout(3000);
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(2000);
        await page.waitForSelector('#username', {visible: true});
        await cas.loginWith(page);
        await page.waitForTimeout(3000);
        await cas.logPage(page);
        await cas.log("Fetching Scratch codes from /cas/actuator...");
        let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.log(`Using scratch code ${scratch} to login...`);
        await cas.screenshot(page);
        await cas.type(page,'#token', scratch);
        await cas.pressEnter(page);
        await page.waitForNavigation();
        await cas.logPage(page);
        await page.waitForTimeout(3000);
        await cas.screenshot(page);
        await cas.assertInnerText(page, "#principal", "casuser");
        await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa");
        await browser.close();
        await cleanUp();
        await cas.log('Cleanup done');
    }, async error => {
        await cleanUp();
        await cas.log(error);
        throw error;
    })
})();

