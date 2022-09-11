const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000);
    await cas.assertParameter(page, "ticket");

    console.log("Trying delegated authentication to activate access strategy");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, 'li #CASClient');
    await cas.click(page, "#CASClient");
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    let response = await cas.loginWith(page, "casuser", "Mellon");
    console.log(`${response.status()} ${response.statusText()}`);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await browser.close();
})();


