const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Establishing SSO session...");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);
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
    await page.waitForTimeout(4000);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    let initialAuthData = authData.AuthnInstant;
    await cas.logg(`Initial authentication instant: ${initialAuthData}`);
    let allCookies = await page.cookies();
    allCookies.forEach(cookie => {
        cas.log(`Deleting cookie ${cookie.name}`);
        page.deleteCookie({
            name : cookie.name,
            domain : cookie.domain
        })
    });
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(3000);

    authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    let nextAuthData = authData.AuthnInstant;
    await cas.logg(`Second authentication instant: ${nextAuthData}`);
    assert(nextAuthData !== initialAuthData);

    await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
    await browser.close();
})();

