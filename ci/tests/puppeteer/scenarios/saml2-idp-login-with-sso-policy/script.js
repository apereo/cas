const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await browser.newPage();

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    // await page.waitForTimeout(1000)

    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    await page.goto("https://samltest.id/start-idp-test/");
    await page.type('input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    // await page.waitForTimeout(1000)
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();

    await page.waitForTimeout(3000)
    
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });

    let uid = await page.$('#username');
    assert(await uid.boundingBox() != null);
    let pswd = await page.$('#password');
    assert(await pswd.boundingBox() != null);

    await browser.close();
})();


