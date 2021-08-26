const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions({ args: ['--lang=de'] }));
    const page = await cas.newPage(browser);
    await page.setExtraHTTPHeaders({
        'Accept-Language': 'de'
    });
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertInnerText(page, "#content #fm1 button[name=submit]", "ANMELDEN")
    await browser.close();
})();
