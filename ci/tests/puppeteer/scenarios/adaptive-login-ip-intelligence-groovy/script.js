
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");

    await cas.closeBrowser(browser);
})();
