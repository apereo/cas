
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas1");
    await cas.loginWith(page);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContentStartsWith(page, "#interruptMessage", "Interrupted");
    await cas.assertCookie(page);
    await cas.assertCookie(page, false, "CASINTERRUPT");

    await cas.click(page, "#continue");
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page, true, "CASINTERRUPT");
    await cas.closeBrowser(browser);
})();
