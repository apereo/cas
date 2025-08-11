
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Verifying SSO policy with casuser");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page, service);
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);
    
    await cas.log("Verifying SSO policy with casblock");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "casblock");
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page, service);
    await cas.assertCookie(page, false);
    await cas.loginWith(page, "casblock");
    await cas.sleep(3000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);
    
    await cas.closeBrowser(browser);
})();
