
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");

    await cas.loginWith(page);
    await page.url();
    await cas.logPage(page);
    await cas.assertTicketParameter(page);
    await browser.close();
})();
