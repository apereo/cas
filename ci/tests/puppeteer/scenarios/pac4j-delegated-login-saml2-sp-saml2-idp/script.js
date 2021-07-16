const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const cas = require('../../cas.js');
const path = require('path');
const axios = require('axios');
const https = require('https');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    instance
        .get('https://localhost:8443/cas/sp/metadata')
        .then(res => {
            assert(res.status === 200)
        })
        .catch(error => {
            throw 'Operation failed to capture metadata';
        })
    instance
        .get('https://localhost:8443/cas/sp/idp/metadata')
        .then(res => {
            assert(res.status === 200)
        })
        .catch(error => {
            throw 'Operation failed to capture metadata';
        })

    console.log("Upload CAS SP metadata...")
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)
    let fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/sp-metadata.xml');
    console.log("SP Metadata file: " + metadata);
    await fileElement.uploadFile(metadata);
    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(2000)

    console.log("Upload CAS IDP metadata...")
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)
    fileElement = await page.$("input[type=file]");
    metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("IDP Metadata file: " + metadata);
    await fileElement.uploadFile(metadata);
    await page.waitForTimeout(1000)

    await cas.click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page,'input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    await page.waitForTimeout(1000)
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, 'li #SAML2Client')
    await cas.click(page, "li #SAML2Client")
    await page.waitForTimeout(6000)

    await cas.loginWith(page, "morty", "panic");
    await page.waitForTimeout(3000)

    await cas.click(page, "input[name='_eventId_proceed']")
    await page.waitForTimeout(3000)

    console.log("Checking for page URL...")
    console.log(await page.url())
    await page.waitForTimeout(3000)

    const header = await cas.textContent(page, "div.entry-content p");
    assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();
