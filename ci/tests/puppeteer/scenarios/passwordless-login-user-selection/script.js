const cas = require("../../cas.js");

async function authenticateWithPassword(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.assertElementDoesNotExist(page, "#password");
    await cas.type(page, "#username", "caspassword");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertVisibility(page, "#fmPassword");
    await cas.assertInvisibility(page, "#fmPasswordlessMfa");
    await cas.assertInvisibility(page, "#fmMfa");
    await cas.assertInvisibility(page, "#fmDelegation");

    await cas.submitForm(page, "#fmPassword");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#password");
    await cas.type(page, "#password", "Mellon");
    await cas.pressEnter(page);
    await cas.sleep(1000);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, caspassword, have successfully logged in");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await context.close();
}

async function authenticateWithDelegation(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.assertElementDoesNotExist(page, "#password");
    await cas.type(page, "#username", "casdelegation");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertInvisibility(page, "#fmPassword");
    await cas.assertInvisibility(page, "#fmPasswordlessMfa");
    await cas.assertInvisibility(page, "#fmMfa");
    await cas.assertVisibility(page, "#fmDelegation");

    await cas.submitForm(page, "#fmDelegation");
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await context.close();
}

async function authenticateWithPasswordlessToken(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.assertElementDoesNotExist(page, "#password");
    await cas.type(page, "#username", "castoken");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertVisibility(page, "#fmPasswordlessMfa");
    await cas.assertInvisibility(page, "#fmPassword");
    await cas.assertInvisibility(page, "#fmMfa");
    await cas.assertInvisibility(page, "#fmDelegation");

    await cas.submitForm(page, "#fmPasswordlessMfa");
    await cas.sleep(2000);

    await cas.assertVisibility(page, "#token");
    await cas.sleep(1000);

    const code = await cas.extractFromEmail(browser);

    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await context.close();
}

async function authenticateWithMfa(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.assertElementDoesNotExist(page, "#password");
    await cas.type(page, "#username", "casmfa");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(2000);
    await cas.assertVisibility(page, "#fmMfa");
    await cas.assertVisibility(page, "#fmPasswordlessMfa");
    await cas.assertInvisibility(page, "#fmPassword");
    await cas.assertInvisibility(page, "#fmDelegation");

    await cas.submitForm(page, "#fmMfa");
    await cas.sleep(2000);

    await cas.assertVisibility(page, "#token");
    await cas.sleep(2000);

    const code = await cas.extractFromEmail(browser);

    await cas.type(page, "#token", code);
    await cas.sleep(1000);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);

    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await context.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await authenticateWithPassword(browser);
    await authenticateWithDelegation(browser);
    await authenticateWithPasswordlessToken(browser);
    await authenticateWithMfa(browser);
    await cas.closeBrowser(browser);
})();
