const cas = require("../../cas.js");

const CHANGE_PASSWORD_URL = "https://localhost:8443/cas/login?doChangePassword=true&locale=en";

async function changePassword(page, password) {
    await cas.goto(page, CHANGE_PASSWORD_URL);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h2", "Hello, casuser. You must change your password.");
    await cas.type(page, "#password", password);
    await cas.type(page, "#confirmedPassword", password);
    await cas.pressEnter(page);
    await cas.sleep(6000);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, CHANGE_PASSWORD_URL);
    await cas.assertInnerText(page, "#content h2", "Password Reset Failed");

    await cas.gotoLogin(page);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(2000);
    }
    await cas.assertCookie(page);
    await cas.goto(page, CHANGE_PASSWORD_URL);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h2", "Hello, casuser. You must change your password.");
    await cas.attributeValue(page, ".generate-password", "title",  "Generate password");

    const firstPassword = `Jv!e0mKD&${await cas.randomWord(8, false, true)}`;
    const secondPassword = `Rq7#mZp2!${await cas.randomWord(8, false, true)}`;

    await cas.log("Changing the password to a new value is accepted and recorded in the password history");
    await changePassword(page, firstPassword);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");

    await cas.log("Changing the password to another new value is accepted");
    await changePassword(page, secondPassword);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");

    await cas.log("Reusing a password found in the history is refused");
    await changePassword(page, firstPassword);
    await cas.assertInnerText(page, "#pwdmain h2", "Hello, casuser. You must change your password.");
    await cas.assertVisibility(page, "#passwordManagementForm .acct-banner-error");
    await cas.assertInnerTextContains(page, "#passwordManagementForm .acct-banner-error p", "Could not update the account password");
    await cas.closeBrowser(browser);
})();
