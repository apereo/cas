const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("http://localhost:8080/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertTicketGrantingCookie(page)
    await page.goto("http://localhost:8080/cas/logout");
    await cas.assertTicketGrantingCookie(page, false)

    await browser.close();
})();
