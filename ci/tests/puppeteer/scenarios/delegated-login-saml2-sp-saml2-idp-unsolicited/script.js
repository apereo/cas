const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);

        await cas.doGet('https://localhost:8443/cas/sp/metadata', res => assert(res.status === 200), () => {
            throw 'Operation failed to capture metadata';
        });

        await cas.doGet('https://localhost:8443/cas/sp/idp/metadata', res => assert(res.status === 200), () => {
            throw 'Operation failed to capture metadata';
        });

        const spEntityId = "cas:apereo:pac4j:saml";
        await cas.goto(page, `http://localhost:9443/simplesaml/saml2/idp/SSOService.php?spentityid=${spEntityId}`);
        await page.waitForTimeout(2000);

        await cas.screenshot(page);
        await cas.loginWith(page, "user1", "password");
        await page.waitForTimeout(2000);

        await cas.log("Checking for page URL...");
        await cas.log(await page.url());
        await cas.screenshot(page);
        await cas.assertTicketParameter(page);

        await cas.screenshot(page);
        await cas.goto(page, "https://localhost:8443/cas/login");
        await cas.assertCookie(page);

        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    } finally {
        await browser.close();
    }
})();
