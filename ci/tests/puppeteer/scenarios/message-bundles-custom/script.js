const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    await cas.assertInnerText(page, "#sidebar div p", "Stay safe!");
    await cas.assertInnerText(page, "#login-form-controls h3 span", "Welcome to CAS");
    await browser.close();
})();
