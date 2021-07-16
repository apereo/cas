const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

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

    await cas.assertVisibility(page, 'li #OktaOidcClient')
    await cas.click(page, "li #OktaOidcClient")
    await page.waitForTimeout(3000)

    await cas.loginWith(page, "info@fawnoos.com", "QFkN&d^bf9vhS3KS49",
        "#okta-signin-username", "#okta-signin-password");
    await page.waitForTimeout(4000)

    const header = await cas.textContent(page, "div.entry-content p");
    assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();
