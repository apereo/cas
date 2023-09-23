const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await page.waitForTimeout(1000);
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        res => {

        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" });
    await browser.close();
})();
