const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Sending SP passive authentication request...");
    let response = await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    console.log("Establishing SSO session...");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertCookie(page);

    console.log("Sending SP passive authentication request with single sign-on session...");
    response = await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    console.log(authData);
    
    await page.waitForTimeout(1000);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();

})();
