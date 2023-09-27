const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

async function verifyWithoutService() {
    const browser1 = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser1);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await browser1.close();

    const browser2 = await puppeteer.launch(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page2, "casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page2, "#loginErrorsPanel p",
        "You cannot login at this time, since you have another active single sign-on session in progress");
    await browser2.close();
}

async function verifyWithService() {
    const browser1 = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser1);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.loginWith(page);
    await browser1.close();

    const browser2 = await puppeteer.launch(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.goto("https://localhost:8443/cas/login?service=https://localhost:9859/anything/1");
    await cas.loginWith(page2, "casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page2, "#loginErrorsPanel p",
        "You cannot login at this time, since you have another active single sign-on session in progress");
    await browser2.close();
}

(async () => {
    await verifyWithoutService();
    await verifyWithService();
})();
