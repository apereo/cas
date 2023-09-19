const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await browser.close();
})();
