const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");

    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await cas.assertTicketGrantingCookie(page);

    await page.goto("https://localhost:8443/cas/logout");

    await cas.assertInnerText(page, "#content h2", "Do you, casuser, want to log out completely?")
    await cas.assertVisibility(page, '#logoutButton')
    await cas.assertVisibility(page, '#divServices')
    await cas.assertVisibility(page, '#servicesTable')
    await cas.submitForm(page, "#fm1");

    let url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://localhost:8443/cas/logout")

    await page.waitForTimeout(1000)
    await cas.assertNoTicketGrantingCookie(page);

    console.log("Logout with redirect...")
    await page.goto("https://localhost:8443/cas/logout?url=https://github.com/apereo/cas");
    await cas.submitForm(page, "#fm1");
    url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://github.com/apereo/cas")

    console.log("Logout with unauthorized redirect...")
    await page.goto("https://localhost:8443/cas/logout?url=https://google.com");
    await cas.submitForm(page, "#fm1");
    url = await page.url()
    await page.waitForTimeout(1000)
    console.log(`Page url: ${url}`)
    assert(url.toString().startsWith("https://localhost:8443/cas/logout"))

    await browser.close();
})();
