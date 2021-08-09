const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "unknown+casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "You are not authorized to impersonate");

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged into the Central Authentication Service");

    await browser.close();
})();
