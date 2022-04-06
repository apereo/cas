const puppeteer = require('puppeteer');

const cas = require('../../cas.js');

(async () => {
    const browser1 = await puppeteer.launch(cas.browserOptions());
    const page1 = await cas.newPage(browser1);
    await page1.goto("https://localhost:8443/cas/login");
    await page1.waitForTimeout(1000)
    await cas.assertVisibility(page1, 'li #CasClient')

    const browser2 = await puppeteer.launch(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page2.waitForTimeout(1000)
    await cas.assertVisibility(page2, 'li #CasClient')

    await cas.click(page1, "li #CasClient")
    await page1.waitForNavigation();

    await cas.click(page2, "li #CasClient")
    await page2.waitForNavigation();

    await cas.loginWith(page1, "casuser", "Mellon");
    await page1.waitForTimeout(1000)

    await cas.assertMissingParameter(page1, "service");
``
    await browser1.close();
    await browser2.close();
})();
