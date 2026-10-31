const cas = require("../../cas.js");
const assert = require("assert");

const QR_DEVICES = "https://localhost:8443/cas/actuator/qrDevices";

async function verifySignInActivity(page) {
    await cas.click(page, "#linkSignInActivity");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#divSignInActivity");
    const outcomes = await page.$$eval("#signInActivityList [data-field=outcome]", (items) => items.map((item) => item.innerText.trim()));
    await cas.logg(outcomes);
    assert(outcomes.includes("Signed in"));
    assert(outcomes.includes("Failed attempt"));
}

async function verifyAcceptableUsagePolicy(page) {
    await cas.click(page, "#linkAcceptableUsagePolicy");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#aupStatus", "Accepted");
    await cas.assertVisibility(page, "#aupPolicyText");
}

async function verifySurrogateAccounts(page) {
    await cas.click(page, "#linkSurrogateAccounts");
    await cas.sleep(1000);
    const accounts = await page.$$eval("#surrogateAccountsList [data-field=account]", (items) => items.map((item) => item.innerText.trim()));
    await cas.logg(accounts);
    assert.deepEqual(accounts, ["banderson", "user3"]);
}

async function verifyQRDevices(page) {
    await cas.click(page, "#linkQRDevices");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#qrDevicesList .acct-mono", "account-profile-device");
    await cas.click(page, "button[name=removeQRDevice]");
    await cas.sleep(1000);
    await cas.click(page, ".swal2-confirm");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#infoMessages", "The device was removed.");
    await cas.assertElementDoesNotExist(page, "#linkQRDevices");
    const devices = JSON.parse(await cas.doRequest(`${QR_DEVICES}/casuser`, "GET", {"Accept": "application/json"}));
    assert(devices.length === 0);
}

async function verifyContactMethods(page, context) {
    await cas.click(page, "#linkContactMethods");
    await cas.sleep(1000);
    const values = await page.$$eval("#simpleMfaContactMethodsList [data-field=value]", (items) => items.map((item) => item.innerText.trim()));
    await cas.logg(values);
    assert.deepEqual(values, ["casuser@example.org", "+1-555-0100"]);

    await cas.click(page, "#registerContactEmail");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#fmRegister");
    await cas.assertVisibility(page, "#cancel");
    await cas.type(page, "#email", "casuser-alt@example.org");
    await cas.click(page, "#registerButton");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);

    const token = await cas.extractFromEmail(context);
    await cas.type(page, "#token", token.trim());
    await cas.click(page, "#registerButton");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#infoMessages", "Your email address is registered for one-time codes.");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await cas.doRequest(`${QR_DEVICES}/casuser/account-profile-device`, "POST", {}, 204);

    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword");
    await cas.sleep(1000);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:8443/cas/account");
    await cas.sleep(1000);
    await cas.assertAccessibility(page, "Account profile features");

    await verifySignInActivity(page);
    await verifyAcceptableUsagePolicy(page);
    await verifySurrogateAccounts(page);
    await verifyQRDevices(page);
    await verifyContactMethods(page, context);

    await context.close();
    await cas.closeBrowser(browser);
})();
