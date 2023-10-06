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
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    await browser.close();
})();
