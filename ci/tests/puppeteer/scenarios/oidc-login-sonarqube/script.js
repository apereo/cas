const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://host.k3d.internal/sessions/new?return_to=%2F");
    await cas.waitForTimeout(page, 10000);
    await cas.assertPageTitle(page, "SonarQube");
    await cas.assertInnerText(page, "h1.login-title","Log in to SonarQube");
    await cas.click(page,"a.identity-provider-link");
    await cas.loginWith(page);
    await cas.waitForElement(page, "ul.global-navbar-menu");
    await cas.assertInnerTextContains(page, "ul.global-navbar-menu","Projects");
    await browser.close();
})();
