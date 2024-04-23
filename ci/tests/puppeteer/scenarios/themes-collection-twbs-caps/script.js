
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await page.keyboard.press("CapsLock");
    await page.type("#password", "M");

    await cas.assertVisibility(page, ".caps-warn");
    const caps = await page.$(".caps-warn");
    await cas.log(`CAPSLOCK warning is ${(caps === null ? "" : "NOT")} hidden`);

    await browser.close();
})();
