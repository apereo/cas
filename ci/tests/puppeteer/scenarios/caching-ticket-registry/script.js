const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    for (let i = 0; i < 5; i++) {
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    }
    await cas.closeBrowser(browser);
})();
