const puppeteer = require("puppeteer");

const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const context1 = browser.createBrowserContext();
    const page1 = await cas.newPage(context1);
    await page1.goto("https://localhost:8443/cas/login");
    await cas.sleep(1000);
    await cas.assertVisibility(page1, "li #CasClient");

    const context2 = browser.createBrowserContext();
    const page2 = await cas.newPage(context2);
    await page2.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await cas.sleep(1000);
    await cas.assertVisibility(page2, "li #CasClient");

    await cas.click(page1, "li #CasClient");
    await page1.waitForNavigation();

    await cas.click(page2, "li #CasClient");
    await page2.waitForNavigation();

    await cas.loginWith(page1, "casuser", "Mellon");
    await cas.sleep(1000);

    await cas.assertMissingParameter(page1, "service");

    await context1.close();
    await context2.close();
    await browser.close();
})();
