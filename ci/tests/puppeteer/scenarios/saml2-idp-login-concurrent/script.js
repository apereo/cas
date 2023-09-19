const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    try {
        await cas.log("Sending first authentication request");
        const page = await cas.newPage(browser);
        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await page.waitForTimeout(4000);
        await cas.log(await page.url());

        await cas.log("Sending second authentication request");
        const page2 = await browser.newPage();
        await page2.bringToFront();
        await cas.goto(page2, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=refeds-sp");
        await page2.waitForTimeout(4000);
        await cas.log(page2.url());

        await cas.log("Resuming with first authentication attempt");
        await page.bringToFront();
        await cas.screenshot(page);
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(3000);
        await page.waitForSelector('#table_with_attributes', {visible: true});
        await cas.assertVisibility(page, "#table_with_attributes");
        
        await page2.bringToFront();
        await cas.screenshot(page2);
        await cas.loginWith(page2, "casuser", "Mellon");
        await page2.waitForTimeout(3000);
        await page2.waitForSelector('#table_with_attributes', {visible: true});
        await cas.assertVisibility(page2, "#table_with_attributes");

    } finally {
        // await cas.removeDirectory(path.join(__dirname, '/saml-md'));
        await browser.close();
    }
})();


