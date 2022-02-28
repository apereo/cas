const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8444/cas/logout");
    console.log("Checking for page URL redirecting based on service policy...")
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(2000)
    let url = await page.url();
    console.log(url)
    assert(url.startsWith("https://localhost:8444/cas/login"))
    await cas.loginWith(page, "casuser", "Mellon")
    await page.waitForTimeout(1000)
    await cas.assertTicketParameter(page);
    url = await page.url();
    console.log(url)
    await page.waitForTimeout(1000)

    console.log("Checking for SSO availability of our CAS server...")
    await page.goto("https://localhost:8443/cas/login");
    url = await page.url();
    console.log(url)
    assert(url.startsWith("https://localhost:8443/cas/login"))
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);

    console.log("Checking for SSO availability of external CAS server...")
    await page.goto("https://localhost:8444/cas/login");
    url = await page.url();
    console.log(url)
    assert(url.startsWith("https://localhost:8444/cas/login"))
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page, true, "TGCEXT");

    console.log("Attempting to login based on existing SSO session")
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");
    url = await page.url();
    console.log(url)
    await page.waitForTimeout(1000)
    await cas.assertTicketParameter(page);

    console.log("Removing CAS SSO session")
    await page.goto("https://localhost:8443/cas/logout");

    console.log("Attempting to login for a different 2nd service")
    await page.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    console.log("Checking for page URL...")
    url = await page.url();
    console.log(url)
    await page.waitForTimeout(1000)

    console.log("External CAS server has no SSO, since logout request was propagated from our CAS server")
    await cas.assertVisibility(page, "#username")
    await cas.assertVisibility(page, "#password")

    await browser.close();
})();
