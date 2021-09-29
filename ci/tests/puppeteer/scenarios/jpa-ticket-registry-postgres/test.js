const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const colors = require("colors");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await page.goto("https://localhost:8443/cas/logout");

    let url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://localhost:8443/cas/logout")

    await page.waitForTimeout(1000)
    await cas.assertNoTicketGrantingCookie(page);

    await browser.close();
    console.log(colors.green(`Login test complete.`));
})();
