
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Service has disabled interrupt, but will establish single sign-on session");
    await cas.gotoLogin(page, "https://localhost:9859/get?nointerrupt");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await cas.log("Service has force-execution for interrupt; every attempt must force interrupt");
    for (let i = 1; i <= 3; i++) {
        await cas.gotoLogin(page, "https://localhost:9859/get?interrupt-forced");
        await cas.sleep(3000);
        await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
        await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
        await cas.assertTextContentStartsWith(page, "#interruptMessage", "We interrupted your login");
        await cas.submitForm(page, "#fm1");
        await cas.sleep(3000);
        await cas.assertTicketParameter(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page);
    }
    await cas.closeBrowser(browser);
})();
