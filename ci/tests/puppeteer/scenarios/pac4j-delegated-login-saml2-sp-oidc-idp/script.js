const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));

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
    await page.waitForSelector('div.entry-content p', { visible: true });
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
