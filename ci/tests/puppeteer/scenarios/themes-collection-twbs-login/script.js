
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page);

    await cas.assertCookie(page);

    const title = await page.title();
    await cas.log(title);
    assert(title === "CAS Boostrap Theme Log In Successful");

    await browser.close();
})();
