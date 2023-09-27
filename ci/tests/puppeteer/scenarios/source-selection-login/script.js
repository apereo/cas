const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.assertVisibility(page, '#authnSourceSection');
    await cas.assertInnerText(page, "#JSON-authnSource", "JSON");
    await cas.assertInnerText(page, "#EXAMPLE-authnSource", "EXAMPLE");
    await browser.close();
})();
