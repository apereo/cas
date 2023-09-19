const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const service = "https://apereo.github.io";
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(2000);
    await cas.click(page, "div .idp span");
    await page.waitForTimeout(4000);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await page.waitForTimeout(1000);
    await cas.submitForm(page, "#loginForm");
    await page.waitForTimeout(4000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, '#content h2', "Multifactor Authentication Provider Selection");
    await cas.assertVisibility(page, '#mfa-yubikey');
    await cas.assertVisibility(page, '#mfa-simple');
    await browser.close();
})();
