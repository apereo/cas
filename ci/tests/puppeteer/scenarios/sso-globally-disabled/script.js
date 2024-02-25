const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#username");

    await cas.assertVisibility(page, "li #CASServerOne");
    await cas.click(page, "li #CASServerOne");
    await page.waitForNavigation();

    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(1000);

    const result = new URL(page.url());
    await cas.log(result.searchParams.toString());

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#username");
    
    await browser.close();
})();
