const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);
    await cas.click(page, "li #SAML2Client");
    await page.waitForNavigation();

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, '#content h2', "Multifactor Authentication Provider Selection");
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');

    await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


