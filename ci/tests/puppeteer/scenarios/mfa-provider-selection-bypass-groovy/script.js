const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://google.com");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.log("Selecting mfa-gauth");
    await cas.assertInvisibility(page, '#mfa-gauth');
    await cas.assertInvisibility(page, '#mfa-yubikey');
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    
    await browser.close();
})();
