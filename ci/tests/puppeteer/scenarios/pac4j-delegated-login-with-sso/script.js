const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Create SSO session with external CAS server...")
    await page.goto("https://casserver.herokuapp.com/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    console.log("Start with first application without SSO for CAS server")
    await page.goto("https://localhost:8443/cas/clientredirect?client_name=CASServer&service=https://github.com/apereo/cas");
    await page.waitForTimeout(1000)

    await cas.assertTicketParameter(page);

    console.log("Start with second application with SSO for CAS server")
    await page.goto("https://localhost:8443/cas/clientredirect?client_name=CASServer&service=https://google.com");
    await page.waitForTimeout(1000)

    await cas.assertTicketParameter(page);

    await browser.close();
})();
