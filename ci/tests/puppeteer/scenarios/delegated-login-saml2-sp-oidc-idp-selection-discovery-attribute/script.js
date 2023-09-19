const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await startWithCasSp(page);
    await browser.close();
})();

async function startWithCasSp(page) {
    const service = "https://apereo.github.io";
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.waitForTimeout(1000);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.assertVisibility(page, '#selectProviderButton');
    await cas.submitForm(page, "#providerDiscoveryForm");
    await page.waitForTimeout(1000);
    await cas.type(page, "#username", "casuser");
    
    await cas.submitForm(page, "#discoverySelectionForm");
    await page.waitForTimeout(2000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body.includes('<cas:user>casuser</cas:user>'))
}
