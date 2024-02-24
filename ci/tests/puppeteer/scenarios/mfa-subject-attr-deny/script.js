const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);

    await cas.waitForTimeout(page);
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "Authentication attempt for your account is denied");

    await browser.close();
})();
