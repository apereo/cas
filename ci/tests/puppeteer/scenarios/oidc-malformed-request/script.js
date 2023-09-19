const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await cas.goto(page, "https://localhost:8443/cas/login?service=https://upgrade.badssl.com");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);

    response = await cas.goto(page, "https://localhost:8443/cas/login?service=https://upgrade.badssl.com&client_id=client");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);

    await cas.log("Checking for unauthorized logout redirect...");
    response = await cas.goto(page, "https://localhost:8443/cas/logout?client_id=client&service=https://upgrade.badssl.com");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);
    
    await browser.close();
})();
