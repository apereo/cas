const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions({ args: ['--lang=de'] }));
    const page = await cas.newPage(browser);
    await page.setExtraHTTPHeaders({
        'Accept-Language': 'de'
    });
    await cas.gotoLogin(page);
    await page.waitForTimeout(1000);

    // Assert that submitBtn is displayed in german language.
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");

    // Assert that HTML root node has attribute `lang="de"`
    const node = await page.$('html');
    assert("de" === await node.evaluate(el => el.getAttribute("lang")));

    await browser.close();
})();
