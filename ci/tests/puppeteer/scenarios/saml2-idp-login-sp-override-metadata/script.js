const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(2000);
    await cas.loginWith(page);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    let artifacts = [
        "idp-metadata.xml",
        "idp-encryption.key",
        "idp-signing.key",
        "idp-encryption.crt",
        "idp-signing.crt"
    ];
    artifacts.forEach(art => {
        let pt = path.join(__dirname, `/saml-md/${art}`);
        cas.log(`Deleting ${pt}`);
        fs.rmSync(pt, { force: true });
    });

    await browser.close();
})();


