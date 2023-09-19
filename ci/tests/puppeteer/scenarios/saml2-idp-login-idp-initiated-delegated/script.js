const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function startFlow(context, clientName) {
    const page = await cas.newPage(context);
    const entityId = encodeURI("https://localhost:9859/shibboleth");
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}&CName=${clientName}`;
    await cas.log(`Navigating to ${url} for client ${clientName}`);
    await cas.goto(page, url);
    await page.waitForTimeout(3000);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.log(await page.url());
    await cas.screenshot(page);
    await cas.waitForElement(page, "body");
    const content = JSON.parse(await cas.innerText(page, "body"));
    await cas.log(content);
    assert(content.form.SAMLResponse != null);

    const service = "https://apereo.github.io";
    url = `https://localhost:8443/cas/login?service=${service}`;
    await cas.goto(page, url);
    await page.waitForTimeout(6000);
    await cas.log(await page.url());
    await cas.assertTicketParameter(page);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const providers = ["CasClient", "CasClientFancy", "CasClientNone"];
    for (const provider of providers) {
        const context = await browser.createIncognitoBrowserContext();
        await startFlow(context, provider);
        await context.close();
    }
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


