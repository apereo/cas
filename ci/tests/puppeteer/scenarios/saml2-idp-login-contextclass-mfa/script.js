const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');

async function cleanUp(exec) {
    console.log("Killing SAML2 SP process...");
    exec.kill();
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await cas.removeDirectory(path.join(__dirname, '/saml-sp'));
}

(async () => {
    let samlSpDir = path.join(__dirname, '/saml-sp');
    let idpMetadataPath = path.join(__dirname, '/saml-md/idp-metadata.xml');
    let exec = await cas.launchSamlSp(idpMetadataPath, samlSpDir, ['-DauthnContext=https://refeds.org/profile/mfa']);
    await cas.waitFor('https://localhost:9876/sp/saml/status', async function () {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);
        page.goto("https://localhost:9876/sp")
        await page.waitForTimeout(3000)
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(2000)
        await page.waitForSelector('#username', {visible: true});
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(3000)
        console.log(`Page URL: ${page.url()}`);
        console.log("Fetching Scratch codes from /cas/actuator...");
        let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        console.log(`Using scratch code ${scratch} to login...`);
        await cas.type(page,'#token', scratch);
        await page.keyboard.press('Enter');
        await page.waitForNavigation();
        console.log(`Page URL: ${page.url()}`);
        await page.waitForTimeout(3000)
        await cas.assertInnerText(page, "#principal", "casuser")
        await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa")
        await browser.close();
        await cleanUp(exec);
    }, async function (error) {
        await cleanUp(exec);
        console.log(error);
        throw error;
    })
})();

