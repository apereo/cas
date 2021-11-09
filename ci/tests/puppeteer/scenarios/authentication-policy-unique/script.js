const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser1 = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser1);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await browser1.close();

    const browser2 = await puppeteer.launch(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page2, "casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page2, "#loginErrorsPanel p",
        "You cannot login at this time, since you have another active single sign-on session in progress");
    await browser2.close();
})();
