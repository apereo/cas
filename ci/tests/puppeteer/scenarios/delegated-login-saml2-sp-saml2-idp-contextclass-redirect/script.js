const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');

async function cleanUp(samlSpDir) {
    console.log("Killing SAML2 SP process...");
    await cas.stopSamlSp(samlSpDir);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    console.log('Cleanup done');
}

(async () => {
    let samlSpDir = path.join(__dirname, '/saml-sp');
    let idpMetadataPath = path.join(__dirname, '/saml-md/idp-metadata.xml');
    await cas.launchSamlSp(idpMetadataPath, samlSpDir, ['-DauthnContext=https://refeds.org/profile/mfa']);
    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        try {
            const page = await cas.newPage(browser);
            await cas.goto(page, "https://localhost:9876/sp")
            await page.waitForTimeout(3000)
            await page.waitForSelector('#idpForm', {visible: true});
            await cas.submitForm(page, "#idpForm");
            await page.waitForTimeout(4000)
            await page.waitForSelector('#username', {visible: true});
            await cas.loginWith(page, "user1", "password");
            await page.waitForTimeout(4000)
            console.log("Checking for page URL...")
            console.log(await page.url())
            await page.waitForTimeout(4000)
            await cas.assertInnerText(page, "#principal", "user1@example.com")
            await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa")
        } finally {
            await browser.close();
            await cleanUp(samlSpDir);
        }
    }, async error => {
        await cleanUp(samlSpDir);
        console.log(error);
        throw error;
    })
})();

