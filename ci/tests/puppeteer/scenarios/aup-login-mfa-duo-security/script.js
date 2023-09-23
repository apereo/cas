const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");

    await cas.loginWith(page, "duobypass", "Mellon");
    await page.waitForTimeout(8000);
    
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");

    await cas.assertVisibility(page, 'button[name=submit]');
    await cas.assertVisibility(page, 'button[name=cancel]');

    await cas.click(page, "#aupSubmit");
    await page.waitForNavigation();
    await page.waitForTimeout(2000);

    await cas.assertTicketParameter(page);
    let result = new URL(page.url());
    assert(result.host === "apereo.github.io");

    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);
    await cas.assertCookie(page);
    
    await browser.close();
})();
