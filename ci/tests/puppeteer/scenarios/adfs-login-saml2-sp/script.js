const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const context = await browser.createIncognitoBrowserContext();
    const page = await cas.newPage(context);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);
    await cas.click(page, "div .idp span");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.log(`Page URL: ${page.url()}`);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await page.waitForTimeout(2000);
    await cas.submitForm(page, "#loginForm");
    await page.waitForTimeout(5000);
    await cas.screenshot(page);
    await cas.log(`Page URL: ${page.url()}`);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    await page.waitForTimeout(1000);

    await browser.close();
})();
