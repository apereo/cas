
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    try {
        await cas.log("Sending first authentication request");
        const page = await cas.newPage(browser);
        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await cas.sleep(4000);
        await cas.logPage(page);

        await cas.log("Sending second authentication request");
        const page2 = await browser.newPage();
        await page2.bringToFront();
        await cas.goto(page2, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=refeds-sp");
        await cas.sleep(4000);
        await cas.log(page2.url());

        await cas.log("Resuming with first authentication attempt");
        await page.bringToFront();
        await cas.screenshot(page);
        await cas.loginWith(page);
        await cas.sleep(3000);
        await page.waitForSelector("#table_with_attributes", {visible: true});
        await cas.assertVisibility(page, "#table_with_attributes");
        
        await page2.bringToFront();
        await cas.screenshot(page2);
        await cas.loginWith(page2, "casuser", "Mellon");
        await cas.sleep(3000);
        await page2.waitForSelector("#table_with_attributes", {visible: true});
        await cas.assertVisibility(page2, "#table_with_attributes");
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();

