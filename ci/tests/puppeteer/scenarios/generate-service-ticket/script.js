
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Generating service ticket without SSO");
    await cas.gotoLogin(page, "https://google.com");
    await cas.loginWith(page);

    await cas.assertTicketParameter(page);

    await cas.log("Generating service ticket with SSO");
    await cas.gotoLogin(page, "https://google.com");
    await cas.assertTicketParameter(page);
    
    await cas.closeBrowser(browser);
})();
