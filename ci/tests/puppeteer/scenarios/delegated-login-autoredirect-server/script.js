const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8444/cas/logout");
    await cas.log("Checking for page URL redirecting based on service policy...");
    await cas.gotoLogin(page, "https://apereo.github.io");
    await page.waitForTimeout(2000);
    await cas.logPage(page);
    let url = await page.url();
    assert(url.startsWith("https://localhost:8444/cas/login"));
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);
    url = await page.url();
    await cas.log(url);
    await page.waitForTimeout(1000);

    await cas.log("Checking for SSO availability of our CAS server...");
    await cas.gotoLogin(page);
    url = await page.url();
    await cas.log(url);
    assert(url.startsWith("https://localhost:8443/cas/login"));
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);

    await cas.log("Checking for SSO availability of external CAS server...");
    await cas.goto(page, "https://localhost:8444/cas/login");
    url = await page.url();
    await cas.log(url);
    assert(url.startsWith("https://localhost:8444/cas/login"));
    await page.waitForTimeout(1000);
    await cas.assertCookie(page, true, "TGCEXT");

    await cas.log("Attempting to login based on existing SSO session");
    await cas.gotoLogin(page, "https://apereo.github.io");
    url = await page.url();
    await cas.log(url);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    await cas.log("Removing CAS SSO session");
    await cas.gotoLogout(page);

    await cas.log("Attempting to login for a different 2nd service");
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await cas.log("Checking for page URL...");
    url = await page.url();
    await cas.log(url);
    await page.waitForTimeout(1000);

    await cas.log("External CAS server has no SSO, since logout request was propagated from our CAS server");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await browser.close();
})();
