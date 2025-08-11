
const cas = require("../../cas.js");

async function execLogin(page, uid) {
    await cas.log(`Logging in with user ${uid}`);
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, uid, "Mellon");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await execLogin(page, "casacct1");
    await execLogin(page, "casacct2");
    await cas.closeBrowser(browser);
})();

