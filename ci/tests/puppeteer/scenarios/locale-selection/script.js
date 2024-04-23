
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions({ args: ["--lang=de"] }));
    const page = await cas.newPage(browser);
    await page.setExtraHTTPHeaders({
        "Accept-Language": "de"
    });
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content #fm1 button[name=submitBtn]", "ANMELDEN");
    await cas.attributeValue(page, "html", "lang", "de");
    await browser.close();
})();
