const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await startWithSamlSp(page);
    await startWithCasSp(page);
    await browser.close();
})();

async function startWithCasSp(page) {
    const service = "https://apereo.github.io";
    await page.goto("https://localhost:8443/cas/logout");
    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await cas.assertVisibility(page, '#selectProviderButton')
    await cas.submitForm(page, "#providerDiscoveryForm")
    await page.waitForTimeout(1000)
    await cas.type(page, "#username", "casuser@heroku.org")
    await cas.submitForm(page, "#discoverySelectionForm")
    await page.waitForTimeout(2000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}`);
    console.log(body)
    assert(body.includes('<cas:user>casuser</cas:user>'))
}

async function startWithSamlSp(page) {
    await page.goto("https://localhost:8443/cas/logout");
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));
    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page, 'input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    await page.waitForTimeout(1000)
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#selectProviderButton')
    await cas.submitForm(page, "#providerDiscoveryForm")
    await page.waitForTimeout(1000)
    await cas.type(page, "#username", "casuser@example.org")
    await cas.submitForm(page, "#discoverySelectionForm")
    await page.waitForTimeout(2000)
    await cas.loginWith(page, "info@fawnoos.com", "QFkN&d^bf9vhS3KS49",
        "#okta-signin-username", "#okta-signin-password");
    await page.waitForSelector('div.entry-content p', {visible: true});
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");
    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
}
