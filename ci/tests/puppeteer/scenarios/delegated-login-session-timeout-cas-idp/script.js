const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(2000);
    await cas.assertVisibility(page, 'li #CasClient');
    await cas.screenshot(page);
    await cas.click(page, "li #CasClient");
    await page.waitForNavigation();
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    let url = await page.url();
    await cas.log(`Page URL: ${url}`);
    await cas.assertParameter(page, "ticket");
    await cas.assertParameter(page, "client_name");
    assert(url.includes("https://localhost:8443/cas/login"));
    await browser.close();
})();


