const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Trying first app with a fancy theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.click(page, "#publicWorkstation");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page, false);
    await cas.assertVisibility(page, '#username');
    await cas.assertVisibility(page, '#password');

    await browser.close();
})();
