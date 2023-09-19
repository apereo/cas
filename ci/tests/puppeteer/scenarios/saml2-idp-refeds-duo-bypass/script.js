const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await cas.log('Cleanup done');
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        try {
            await cas.goto(page, "https://localhost:9876/sp");
            await page.waitForTimeout(3000);
            await page.waitForSelector('#idpForm', {visible: true});
            await cas.submitForm(page, "#idpForm");
            await page.waitForTimeout(3000);
            await page.waitForSelector('#username', {visible: true});

            await cas.loginWith(page, "duobypass", "Mellon");
            await page.waitForTimeout(3000);

            await cas.log("Checking for page URL...");
            await cas.log(await page.url());
            await page.waitForTimeout(3000);
            await cas.assertInnerText(page, "#principal", "casuser@example.org");
            await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa")
        } finally {
            await browser.close();
            await cleanUp();
        }
    }, async error => {
        await cleanUp();
        await cas.log(error);
        throw error;
    })
})();

