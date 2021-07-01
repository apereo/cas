const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/Sample-1/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    await page.waitForTimeout(1000)

    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page,'input[name=\'entityID\']', "https://cas.apereo.org/custom/idp/21c826665039536e");
    await page.waitForTimeout(1000)
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();

    await page.waitForTimeout(5000)

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000)
    
    const header = await cas.textContent(page, "div.entry-content p");
    assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));

    let artifacts = [
        "idp-metadata.xml",
        "idp-encryption.key",
        "idp-signing.key",
        "idp-encryption.crt",
        "idp-signing.crt"
    ]
    artifacts.forEach(art => {
        let pt = path.join(__dirname, '/saml-md/' + art);
        console.log("Deleting " + pt)
        fs.rmSync(pt, { force: true });
    })

    await browser.close();
})();


