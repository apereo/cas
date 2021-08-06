const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    // await page.goto("https://localhost:8443/cas/login");
    // await cas.loginWith(page, "casuser", "Mellon");

    // await page.goto("https://samltest.id/upload.php");
    // await page.waitForTimeout(1000)
    //
    // const fileElement = await page.$("input[type=file]");
    // let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    // console.log("Metadata file: " + metadata);
    //
    // await fileElement.uploadFile(metadata);
    //
    // await cas.click(page, "input[name='submit']")
    // await page.waitForNavigation();
    //
    // await page.goto("https://samltest.id/start-idp-test/");
    // await cas.type(page,'input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    // await cas.click(page, "input[type='submit']")
    // await page.waitForNavigation();
    //
    // await page.waitForSelector('div.entry-content p', { visible: true });
    // let header = await cas.textContent(page, "div.entry-content p");
    // assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));
    //
    // let entityId = "https://httpbin.org/shibboleth";
    // let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    // url += `?providerId=${entityId}`;
    // url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    // await page.goto(url);
    // await page.waitForTimeout(1000)
    // header = await cas.innerText(page, '#content h2');
    // assert(header === "Application Not Authorized to Use CAS")
    //
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdir(metadataDir, { recursive: true }, () => {});
    await browser.close();
})();

