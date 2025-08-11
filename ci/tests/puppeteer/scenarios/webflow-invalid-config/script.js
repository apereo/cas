
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    // await cas.sleep(2000)
    await page.$eval("input[name=_eventId]", (el) => el.value = "unknown");
    await cas.loginWith(page);
    await cas.assertInnerText(page, "#content h2", "Invalid/Unknown Webflow Configuration");
    await cas.assertInnerTextStartsWith(page, "#content p", "You are seeing this error because");
    await cas.assertInnerTextStartsWith(page, "#exceptionMessage", "No transition found");
    await cas.closeBrowser(browser);
})();
