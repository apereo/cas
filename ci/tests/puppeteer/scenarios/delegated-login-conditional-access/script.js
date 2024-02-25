const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertParameter(page, "ticket");

    await cas.log("Trying delegated authentication to activate access strategy");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "li #CASClient");
    await cas.click(page, "#CASClient");
    await page.waitForNavigation();
    await cas.sleep(1000);
    const response = await cas.loginWith(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Unauthorized Access");
    await cas.assertTextContentStartsWith(page, "#content div p", "Either the authentication request was rejected/cancelled");
    assert(response.status() === 401);
    await browser.close();
})();

