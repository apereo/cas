
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    
    await cas.closeBrowser(browser);
})();
