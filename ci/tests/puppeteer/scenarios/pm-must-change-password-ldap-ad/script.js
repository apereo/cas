
const cas = require("../../cas.js");

const PASSWORD = "P@ssw0rd";

async function loginAs(page, user, password = PASSWORD) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, user, password);
    await cas.sleep(2000);
}

(async () => {
    await cas.log(`Running with LDAP authentication type ${process.env.SCENARIO_VARIATION}`);
    await cas.dockerExec("samba", ["bash", "-c",
        `samba-tool user setpassword --filter=cn=mustchangepassword --newpassword=${PASSWORD} --must-change-at-next-login`]);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await loginAs(page, "aburr");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await loginAs(page, "aburr", "Bad-P@ssw0rd");
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "Authentication attempt has failed");
    await cas.assertCookie(page, false);

    await loginAs(page, "disableduser");
    await cas.assertInnerText(page, "#view-accountdisabled-heading", "This account has been disabled.");
    await cas.assertCookie(page, false);

    await loginAs(page, "expireduser");
    await cas.assertInnerText(page, "#pwdmain h2", "Hello, expireduser. Your password has expired.");
    await cas.assertCookie(page, false);

    await loginAs(page, "expirestomorrow");
    await cas.assertTextContent(page, "#content h1", "Authentication Succeeded with Warnings");
    await cas.assertInnerTextContains(page, "#content ul", "Your password expires in 1 day(s).");
    await cas.assertVisibility(page, "#changePassword");
    await cas.submitForm(page, "#form");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await loginAs(page, "mustchangepassword");
    await cas.assertInnerText(page, "#pwdmain h2", "Hello, mustchangepassword. You must change your password.");
    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.closeBrowser(browser);
})();
