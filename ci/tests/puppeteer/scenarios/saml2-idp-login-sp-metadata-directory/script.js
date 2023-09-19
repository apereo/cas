const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let entityIds = [
        "sp1:example",
        "sp2:example"
    ];
    for (const entityId of entityIds) {
        let url = `https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO?providerId=${entityId}`;
        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.screenshot(page);
        await page.waitForTimeout(2000);
        await cas.loginWith(page, "casuser", "Mellon");
        await cas.waitForElement(page, "body");
        const content = JSON.parse(await cas.innerText(page, "body"));
        await cas.log(content);
        assert(content.form.SAMLResponse != null);
        await cas.goto(page, "https://localhost:8443/cas/logout");
    }
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


