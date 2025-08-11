const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

async function registerGoogleAuthenticatorAccount() {
    const template = path.join(__dirname, "gauth.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import record:\n${body}`);
    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository/import", "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);
}

async function deleteGoogleAuthenticatorAccounts() {
    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository", "DELETE");
}

async function passwordResetFlowMfaWithoutRegisteredDevice(browser) {
    await deleteGoogleAuthenticatorAccounts();
    
    const context = await browser.createBrowserContext();
    try {
        const page = await cas.newPage(context);
        await cas.gotoLogin(page);
        await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
        await cas.click(page, "#forgotPasswordLink");
        await cas.sleep(2000);

        await cas.log("Trying to reset password without a registered device...");
        await cas.type(page, "#username", "casuser");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.screenshot(page);
        await cas.sleep(1000);
        await cas.assertInnerTextStartsWith(page, "#fm1 section div.alert p", "Your MFA provider has denied your attempt");

        await registerGoogleAuthenticatorAccount();

        await cas.log("Trying to reset password with a registered device...");
        await cas.type(page, "#username", "casuser");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.screenshot(page);

        const link = await cas.extractFromEmail(context);
        assert(link !== undefined);
        await cas.goto(page, link);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
        await cas.type(page, "#q0", "Salad");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);

        await cas.assertInvisibility(page, "div#deviceRegistrationDiv #registerButton");
        await cas.assertInvisibility(page, "div#deviceRegistrationDiv #confirm-reg-dialog");
        await cas.sleep(1000);

        await cas.click(page, "#selectDeviceButton");
        await cas.waitForNavigation(page);
        await cas.assertInvisibility(page, "#delButton-1");
        await cas.assertVisibility(page, "#useButton-1");
        await cas.click(page, "#useButton-1");
        await cas.waitForNavigation(page);
        await cas.assertVisibility(page, "input#token");

        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.log(`Using scratch code ${scratch} to login...`);
        await cas.type(page, "#token", scratch);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
        await cas.attributeValue(page, ".generate-password", "title", "Generate password");
    } finally {
        await deleteGoogleAuthenticatorAccounts();
        await context.close();
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        await passwordResetFlowMfaWithoutRegisteredDevice(browser);
    } finally {
        await deleteGoogleAuthenticatorAccounts();
        await cas.closeBrowser(browser);
    }
})();
