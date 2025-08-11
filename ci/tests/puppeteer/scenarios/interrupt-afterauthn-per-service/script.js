
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/1");
    await cas.loginWith(page, "testuser", "testuser");
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertCookie(page, false);

    await cas.gotoLogin(page, "https://localhost:9859/anything/2");
    await cas.loginWith(page, "testuser", "testuser");
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertCookie(page, false);

    await cas.closeBrowser(browser);
})();
