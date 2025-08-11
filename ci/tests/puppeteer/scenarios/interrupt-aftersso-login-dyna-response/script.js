
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/1");
    await cas.loginWith(page);

    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "Hello from system");

    await cas.submitForm(page, "#fm1");
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "Hello from admin");
    await cas.submitForm(page, "#fm1");
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    
    await cas.closeBrowser(browser);
})();
