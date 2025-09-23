
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://cn.admin.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await cas.gotoLogin(page, "https://credtype.userpswd.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await cas.gotoLogin(page);
    await submitLogin(page);
    await cas.gotoLogin(page, "https://credtype.userpswd.example.com");
    await assertFailure(page);
    await cas.closeBrowser(browser);
})();

async function submitLogin(page) {
    await cas.loginWith(page);
}

async function assertFailure(page) {
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
    await cas.sleep(1000);
}
