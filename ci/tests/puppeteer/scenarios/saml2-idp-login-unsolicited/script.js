const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

async function unsolicited(page, target) {
    const entityId = "https://samltest.id/saml/sp";

    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += `&target=${target}`;

    console.log("Navigating to " + url);
    await page.goto(url);
    await page.waitForNavigation();

    const result = await page.url()
    console.log(`Page url: ${result}`)
    assert(result.includes(target));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://samltest.id/upload.php");

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    await unsolicited(page, "https://apereo.github.io");
    await page.waitForTimeout(1000)

    await unsolicited(page, "https://github.com/apereo/cas");
    await page.waitForTimeout(1000)

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdir(metadataDir, { recursive: true }, () => {});
    await browser.close();
})();
