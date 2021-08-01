const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await cas.doGet('https://localhost:8443/cas/sp/metadata', function(res) {
        assert(res.status === 200)
    }, function(error) {
        throw 'Operation failed to capture metadata';
    });

    await cas.doGet('https://localhost:8443/cas/sp/idp/metadata', function(res) {
        assert(res.status === 200)
    }, function(error) {
        throw 'Operation failed to capture metadata';
    });
    
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
    await page.waitForSelector('div.entry-content p', { visible: true });
    const header = await cas.textContent(page, "div.entry-content p");
    assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdir(metadataDir, { recursive: true }, () => {});
    
    await browser.close();
})();
