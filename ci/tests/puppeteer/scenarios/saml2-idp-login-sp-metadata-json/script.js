const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const entityId = "https://httpbin.org/anything";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    console.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.screenshot(page);
    await page.waitForTimeout(2000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000);
    const content = JSON.parse(await cas.innerText(page, "body"));
    console.log(content);
    assert(content.form.SAMLResponse != null);
    await browser.close();
})();


