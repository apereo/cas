const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await login(page, "https://apereo.github.io", "mfa-duo");
    await page.waitForTimeout(2000);

    await login(page, "https://localhost:9859/anything/open", "mfa-duo-alt");
    await page.waitForTimeout(2000);

    await browser.close();
})();

async function login(page, service, providerId) {
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page, false);
    await cas.log(`Trying with service ${service}`);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(2000);
    await cas.loginWith(page, "duobypass", "Mellon");
    await page.waitForTimeout(4000);
    await cas.screenshot(page);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.screenshot(page);
    await page.waitForSelector("#content", {visible: true});
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", providerId);
    await cas.screenshot(page);
}
