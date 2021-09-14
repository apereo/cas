const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));
    
    const entityId = encodeURI("https://samltest.id/saml/sp");
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}&CName=CasClient`;

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    await page.waitForTimeout(3000)
    await cas.screenshot(page);
    await cas.loginWith(page, "casuser", "Mellon");
    console.log(await page.url())
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await page.waitForSelector('div.entry-content p', { visible: true });
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");
    await browser.close();
})();


