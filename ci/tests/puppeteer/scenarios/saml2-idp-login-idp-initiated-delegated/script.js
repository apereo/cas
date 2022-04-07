const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityId = encodeURI("https://httpbin.org/shibboleth");
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}&CName=CasClient`;

    console.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await page.waitForTimeout(3000)
    await cas.loginWith(page, "casuser", "Mellon");
    console.log(await page.url())
    await page.waitForTimeout(3000)
    const content = JSON.parse(await cas.innerText(page, "body"));
    console.log(content)
    assert(content.form.SAMLResponse != null)
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


