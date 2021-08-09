const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

async function unsolicited(page, target) {
    const entityId = "https://samltest.id/saml/sp";

    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += `&target=${target}`;

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    await page.waitForNavigation();

    const result = await page.url()
    console.log(`Page url: ${result}`)
    assert(result.includes(target));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    await unsolicited(page, "https://apereo.github.io");
    await page.waitForTimeout(1000)

    await unsolicited(page, "https://github.com/apereo/cas");
    await page.waitForTimeout(1000)
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
