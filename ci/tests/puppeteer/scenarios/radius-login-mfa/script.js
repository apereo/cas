const cas = require("../../cas.js");

async function submitToken(page, token) {
    await cas.type(page, "#token", token);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("RADIUS rejects a wrong password, and static users are switched off");
        await cas.gotoLogin(page);
        await cas.loginWith(page, "casuser", "WrongPassword");
        await cas.assertCookie(page, false);
        await cas.assertVisibility(page, "#loginErrorsPanel");

        await cas.logg("RADIUS accepts the right password as the primary authentication");
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.gotoLogout(page);

        await cas.logg("RADIUS multifactor authentication rejects a wrong token and accepts the right one");
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-radius");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.assertVisibility(page, "#token");
        await submitToken(page, "WrongToken");
        await cas.assertCookie(page, false);
        await cas.assertVisibility(page, "#token");
        await submitToken(page, "Mellon");
        await cas.assertCookie(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    } finally {
        await cas.closeBrowser(browser);
    }
})();
