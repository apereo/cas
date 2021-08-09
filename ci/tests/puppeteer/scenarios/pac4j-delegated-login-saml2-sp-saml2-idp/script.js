const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await cas.doGet('https://localhost:8443/cas/sp/metadata', function(res) {
        assert(res.status === 200)
    }, function() {
        throw 'Operation failed to capture metadata';
    });

    await cas.doGet('https://localhost:8443/cas/sp/idp/metadata', function(res) {
        assert(res.status === 200)
    }, function() {
        throw 'Operation failed to capture metadata';
    });
    
    console.log("Upload CAS SP metadata...")
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/sp-metadata.xml'));

    console.log("Upload CAS IDP metadata...")
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));

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
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
