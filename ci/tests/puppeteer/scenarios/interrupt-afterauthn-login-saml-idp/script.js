const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.screenshot(page);

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);
    let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(3000);

    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertCookie(page, false);
    await cas.assertTextContentStartsWith(page, "#interruptMessage", "We interrupted your login");
    await cas.assertVisibility(page, '#interruptLinks');
    await cas.assertVisibility(page, '#attributesTable');
    await cas.assertVisibility(page, '#field1');
    await cas.assertVisibility(page, '#field1-value');
    await cas.assertVisibility(page, '#field2');
    await cas.assertVisibility(page, '#field2-value');
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(6000);
    await cas.screenshot(page);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
