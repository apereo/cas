const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");

const ACCOUNT_STATUS_PAGES = [
    {user: "locked", password: "locked", name: "Account locked"},
    {user: "disabled", password: "disabled", name: "Account disabled"},
    {user: "expired", password: "expired", name: "Password expired"},
    {user: "mustchange", password: "mustchange", name: "Password must change"},
    {user: "invalidtime", password: "invalidtime", name: "Bad hours"},
    {user: "invalidlocation", password: "invalidlocation", name: "Bad workstation"}
];

/*
 * Reads a scratch code that is still unused from the copy of the Google Authenticator accounts that init.sh made,
 * since the JSON repository writes used codes back to that file.
 */
function unusedScratchCode() {
    const file = path.join(process.env.TMPDIR ?? "/tmp", "template-accessibility-accounts.json");
    const accounts = JSON.parse(fs.readFileSync(file, "utf8"));
    return String(accounts.casuser[1][0].scratchCodes[1][0]);
}

/*
 * Ends the single sign-on session, confirming the logout when CAS asks for it.
 */
async function logout(page) {
    await cas.gotoLogout(page);
    if (await page.$("#logoutButton") !== null) {
        await cas.submitForm(page, "#fm1");
    }
    await cas.assertCookie(page, false);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const blocking = [];
    const audit = async (name) => blocking.push(...await cas.auditAccessibility(page, name));

    await cas.logg("Login, login errors and account status pages");
    await cas.gotoLogin(page);
    await cas.assertVisibility(page, "#username");
    await audit("Login");

    await cas.loginWith(page, "casuser", "BadPassword");
    await cas.assertVisibility(page, "#loginErrorsPanel");
    await audit("Login error");

    for (const status of ACCOUNT_STATUS_PAGES) {
        await cas.gotoLogin(page);
        await cas.loginWith(page, status.user, status.password);
        await cas.assertCookie(page, false);
        await audit(status.name);
    }

    await cas.logg("Password management pages");
    await cas.gotoLogin(page);
    await cas.click(page, "#forgotPasswordLink");
    await cas.waitForElement(page, "#reset #fm1");
    await audit("Forgot password");
    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await audit("Password reset instructions sent");

    await cas.gotoLogin(page);
    await cas.click(page, "#forgotUsernameLink");
    await cas.waitForElement(page, "#reset #fm1");
    await audit("Forgot username");
    await cas.type(page, "#email", "casuser@example.org");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await audit("Username instructions sent");

    await cas.logg("Error pages");
    await cas.gotoLogin(page, "https://unknown.example.net");
    await cas.assertTextContent(page, "#content h2", "Application Not Authorized to Use CAS");
    await audit("Unauthorized service");
    await cas.goto(page, "https://localhost:8443/cas/unknown-page");
    await audit("Page not found");

    await cas.logg("Single sign-on, attribute consent and logout");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await audit("Login success");

    await cas.gotoLogin(page, "https://example.org");
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await audit("Attribute consent");

    await cas.gotoLogout(page);
    await cas.assertVisibility(page, "#logoutButton");
    await audit("Logout confirmation");
    await cas.submitForm(page, "#fm1");
    await cas.assertCookie(page, false);
    await audit("Logout");

    await cas.logg("Google Authenticator registration, token and trusted device pages");
    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page, "gauthuser", "Mellon");
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#confirm");
    await audit("Google Authenticator registration");
    await logout(page);

    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#token");
    await audit("Google Authenticator");
    await cas.type(page, "#token", unusedScratchCode());
    await cas.submitForm(page, "#fm1");
    await cas.waitForElement(page, "#deviceName");
    await audit("Trusted device registration");
    await logout(page);

    await cas.closeBrowser(browser);
    assert(blocking.length === 0, `Serious or critical accessibility violations found: ${blocking.join(", ")}`);
})();
