const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");

    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(1000)

    await cas.assertTicketGrantingCookie(page);

    await page.goto("https://localhost:8443/cas/logout");
    // await page.waitForTimeout(5000)

    const header = await cas.innerText(page, '#content h2');

    assert(header === "Do you, casuser, want to log out completely?")

    await cas.assertVisibility(page, '#logoutButton')

    await cas.assertVisibility(page, '#divServices')

    await cas.assertVisibility(page, '#servicesTable')

    await cas.submitForm(page, "#fm1");

    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://localhost:8443/cas/logout")

    // await page.waitForTimeout(20000)

    await cas.assertNoTicketGrantingCookie(page);

    await browser.close();
})();
