const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.log("Checking for page URL...");
    await cas.logPage(page);
    let url = await page.url();
    assert(url.startsWith("https://localhost:8444/cas/login"));
    await cas.waitForTimeout(page, 1000);
    await cas.loginWith(page);
    await cas.waitForTimeout(page, 2000);
    url = await page.url();
    await cas.log(url);
    assert(url.startsWith(service));
    await cas.assertTicketParameter(page);
    await cas.waitForTimeout(page, 1000);

    await cas.log("Attempting login after SSO...");
    await cas.gotoLogin(page, service);
    url = await page.url();
    await cas.log(url);
    await cas.waitForTimeout(page, 1000);
    await cas.assertTicketParameter(page);

    await browser.close();
})();
