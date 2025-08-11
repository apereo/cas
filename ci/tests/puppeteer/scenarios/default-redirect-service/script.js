
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");
    await cas.loginWith(page);
    
    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://github.com/");
    await cas.closeBrowser(browser);
})();
