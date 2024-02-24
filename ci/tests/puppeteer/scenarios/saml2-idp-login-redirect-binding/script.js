const puppeteer = require("puppeteer");
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await cas.waitForTimeout(page);
        await cas.loginWith(page);
        await cas.waitForTimeout(page);
        await cas.waitForTimeout(page);
        await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
        await cas.assertVisibility(page, "#table_with_attributes");

        const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
        await cas.log(authData);

    } finally {
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    }
    await browser.close();
})();
