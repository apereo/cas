const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://samltest.id/upload.php");
    // await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    // await page.waitForTimeout(1000)

    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();

    // await page.waitForTimeout(1000)

    let entityId = "https://samltest.id/saml/sp";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

    console.log("Navigating to " + url);
    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000)

    await cas.assertPageTitle(page, "CAS - Central Authentication Service");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdir(metadataDir, { recursive: true }, () => {});
    
    await browser.close();
})();


