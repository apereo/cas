const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
    assert(body === "yes\ncasuser\n");

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    await cas.goto(page, `https://localhost:8443/cas/logout`);
    await page.waitForTimeout(2000);
    const content = await page.content();
    assert(content.includes('id="service1"'));
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
