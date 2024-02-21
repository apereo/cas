const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Verifying SSO policy with casuser");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.waitForTimeout(page, 2000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page, service);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);
    
    await cas.log("Verifying SSO policy with casblock");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "casblock");
    await cas.waitForTimeout(page, 2000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page, service);
    await cas.assertCookie(page, false);
    await cas.loginWith(page, "casblock");
    await cas.waitForTimeout(page, 2000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);
    
    await browser.close();
})();
