
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");

    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.sleep(8000);
    
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");

    await cas.assertVisibility(page, "button[name=submit]");
    await cas.assertVisibility(page, "button[name=cancel]");

    await cas.click(page, "#aupSubmit");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);

    await cas.assertTicketParameter(page);
    await cas.logPage(page);
    await cas.assertPageUrlHost(page, "apereo.github.io");

    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertCookie(page);
    
    await cas.closeBrowser(browser);
})();
