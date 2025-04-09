
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    for (let i = 0; i < 3; i++) {
        await cas.log(`Attempt #${i}: waiting for timeout to complete...`);
        await cas.sleep(1000);
        await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
        await cas.assertTicketParameter(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page);
    }
    await cas.sleep(4000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await browser.close();
})();
