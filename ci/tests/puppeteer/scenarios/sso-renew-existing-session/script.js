
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.gotoLogin(page, "https://example.com&renew=true");
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#existingSsoMsg");

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    
    await cas.closeBrowser(browser);
})();
