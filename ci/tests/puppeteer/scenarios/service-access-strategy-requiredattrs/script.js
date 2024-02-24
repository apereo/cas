const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://cn.admin.example.com");
    await cas.loginWith(page);
    await assertFailure(page);

    await cas.gotoLogin(page, "https://credtype.userpswd.example.com");
    await cas.loginWith(page);
    await assertFailure(page);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.gotoLogin(page, "https://credtype.userpswd.example.com");
    await assertFailure(page);

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://required.rejected.example.com");
    await cas.loginWith(page);
    await assertFailure(page);

    await browser.close();
})();

async function assertFailure(page) {
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
    await cas.waitForTimeout(page);
}
