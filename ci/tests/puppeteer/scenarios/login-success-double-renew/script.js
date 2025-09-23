
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await page.focus("#username");
    await cas.pressTab(page);
    await page.focus("#password");
    await cas.pressTab(page);

    await cas.assertVisibility(page, "#usernameValidationMessage");
    await cas.assertVisibility(page, "#passwordValidationMessage");

    await cas.loginWith(page);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    for (let i = 0; i < 2; i++) {
        await cas.gotoLogin(page, undefined, 8443, true);
        await cas.sleep(1000);

        await cas.assertVisibility(page, "#existingSsoMsg");
    }

    await cas.closeBrowser(browser);
})();
