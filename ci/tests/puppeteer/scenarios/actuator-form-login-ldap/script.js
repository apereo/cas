const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/actuator/info");

    await cas.assertInnerText(page, "#content h1", "Login");
    await cas.assertVisibility(page, '#content form[name=fm1]');
    await cas.assertInnerText(page, "#content form[name=fm1] h3", "Enter Username & Password");
    await cas.assertVisibility(page, '#username');
    await cas.assertVisibility(page, '#password');

    let response = await cas.loginWith(page, "actuator", "123456");
    await cas.log(`${response.status()} ${response.statusText()}`);
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    assert(response.status() === 200);
    
    await browser.close();
})();
