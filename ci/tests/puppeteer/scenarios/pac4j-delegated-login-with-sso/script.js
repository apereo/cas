const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Create SSO session with external CAS server...")
    await page.goto("https://localhost:8444/cas/login");
    await page.waitForTimeout(3000)
    await cas.screenshot(page);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    console.log("Start with first application without SSO for CAS server")
    await page.goto("https://localhost:8443/cas/clientredirect?client_name=CASServer&service=https://github.com/apereo/cas");
    await page.waitForTimeout(1000)
    await cas.assertTicketParameter(page);

    console.log("Checking SSO for our CAS server")
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    await browser.close();
})();
