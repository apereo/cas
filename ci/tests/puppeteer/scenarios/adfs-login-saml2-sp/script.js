const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.waitForTimeout(page);
    await cas.click(page, "div .idp span");
    await cas.waitForTimeout(page);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await cas.waitForTimeout(page);
    await cas.submitForm(page, "#loginForm");
    await cas.waitForTimeout(page, 5000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.waitForTimeout(page);
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    await cas.waitForTimeout(page);

    await browser.close();
})();
