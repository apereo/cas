const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.log("Checking for page URL...");
    let url = await page.url();
    await cas.log(url);
    assert(url.startsWith("https://localhost:8444/cas/login"));

    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");

    url = await page.url();
    await cas.log(url);
    assert(url.startsWith("https://apereo.github.io/"));
    await cas.assertTicketParameter(page);
    await page.waitForTimeout(1000);

    await cas.log("Attempting login after SSO...");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    url = await page.url();
    await cas.log(url);
    await cas.assertTicketParameter(page);

    await browser.close();
})();
