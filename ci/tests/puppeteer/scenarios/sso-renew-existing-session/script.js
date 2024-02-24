const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    
    await cas.gotoLogin(page);
    await cas.waitForTimeout(page);
    await cas.assertCookie(page);

    await cas.gotoLogin(page, "https://example.com&renew=true");
    await cas.waitForTimeout(page);

    await cas.assertVisibility(page, "#existingSsoMsg");

    await cas.gotoLogin(page);
    await cas.waitForTimeout(page);
    await cas.assertCookie(page);
    
    await browser.close();
})();
