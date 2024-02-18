const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/1");
    await cas.waitForTimeout(page, 1000);
    await cas.click(page, "#warn");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await cas.assertVisibility(page, "#ignorewarn");
    await cas.waitForTimeout(page, 1000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await cas.waitForTimeout(page, 1000);
    await cas.assertVisibility(page, "#ignorewarn");
    await cas.click(page, "#ignorewarn");
    await cas.waitForTimeout(page, 2000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/3");
    await cas.waitForTimeout(page, 1000);
    await cas.assertInvisibility(page, "#ignorewarn");
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page, false, "CASPRIVACY");
    
    await browser.close();
})();
