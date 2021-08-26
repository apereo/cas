const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/Sample-1/idp-metadata.xml'));

    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page,'input[name=\'entityID\']', "https://cas.apereo.org/custom/idp/21c826665039536e");
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000)

    await page.waitForSelector('div.entry-content p', { visible: true });
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");

    let artifacts = [
        "idp-metadata.xml",
        "idp-encryption.key",
        "idp-signing.key",
        "idp-encryption.crt",
        "idp-signing.crt"
    ]
    artifacts.forEach(art => {
        let pt = path.join(__dirname, `/saml-md/${art}`);
        console.log(`Deleting ${pt}`)
        fs.rmSync(pt, { force: true });
    })

    await browser.close();
})();


