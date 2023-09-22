const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    try {
        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);

        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await page.waitForTimeout(5000);
        await cas.screenshot(page);
        await cas.loginWith(page, "duocode", "Mellon");
        await page.waitForTimeout(5000);
        await cas.screenshot(page);

        await cas.loginDuoSecurityBypassCode(page,"duocode");
        await page.waitForTimeout(5000);
        await cas.screenshot(page);

        await page.waitForSelector('#table_with_attributes', {visible: true});
        await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
        await cas.assertVisibility(page, "#table_with_attributes");
        let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
        console.dir(authData, {depth: null, colors: true});

    } finally {
        await cas.screenshot(page);
        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    }
    await browser.close();

})();
