
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://apereo.github.io#hello-world";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertTicketParameter(page);
    await cas.logPage(page);
    const url = await page.url();
    assert((url.match(/#/g) || []).length === 1);
    const result = new URL(page.url());
    await cas.logg(`URL hash is ${result.hash}`);
    assert(result.hash.startsWith("#hello-world"));
    await browser.close();
})();
