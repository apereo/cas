const puppeteer = require('puppeteer');
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
    await cas.assertAttribute(page, "html", "lang", "de");
    await browser.close();
})();
