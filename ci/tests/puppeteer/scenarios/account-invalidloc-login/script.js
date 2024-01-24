const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "invalidlocation", "invalidlocation");
    await cas.assertInnerText(page, "#content h2", "You cannot log in from this workstation.");
    await browser.close();
})();
