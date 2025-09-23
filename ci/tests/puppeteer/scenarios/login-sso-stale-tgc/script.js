
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    const cookie0 = await buildCookie(page, "TGT-09876", "/cas");
    const cookie1 = await buildCookie(page, "TGT-12345", "/cas/");
    const cookie2 = await buildCookie(page, "TGT-67890", "/");
    const cookie3 = await buildCookie(page, "OtherCookie", "/cas/", "TestCookie");
    await page.setCookie(cookie0, cookie1, cookie2, cookie3);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.loginWith(page);
    const tgc = await cas.assertCookie(page);
    assert(tgc.path === "/cas");
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    tgc.path = "/cas/";
    await page.setCookie(tgc, cookie3);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);
})();

async function buildCookie(page, value, path, name = "TGC") {
    await cas.log(`Adding cookie ${name}:${value}:${path}`);
    return {
        name: name,
        value: value,
        domain: "localhost",
        path: path,
        httpOnly: true,
        secure: true
    };
}
