
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertTextContent(page, "#login h2", "casuser");
    await cas.assertTextContent(page, "#guaInfo", "If you do not recognize this image as yours, do NOT continue.");
    await cas.assertVisibility(page, "#guaImage");
    await cas.submitForm(page, "#fm1");
    await cas.type(page,"#password", "Mellon");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
