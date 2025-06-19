
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

async function removeWebAuthnDevices() {
    await cas.doDelete("https://localhost:8443/cas/actuator/webAuthnDevices/casuser");
}

async function removeAllYubiKeyDevices() {
    await cas.doDelete("https://localhost:8443/cas/actuator/yubikeyAccountRepository");
}

async function importMultifactorTrustedRecord() {
    const template = path.join(__dirname, "device-record.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import device record:\n${body}`);
    const record = JSON.parse(await cas.doRequest("https://localhost:8443/cas/actuator/multifactorTrustedDevices/import", "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body));
    await cas.logg(record);
    return record;
}

async function importWebAuthnDevice() {
    const template = path.join(__dirname, "webauthn-acct.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import device record:\n${body}`);
    await cas.doRequest("https://localhost:8443/cas/actuator/webAuthnDevices/import", "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);
}

async function importYubiKeyDevice() {
    const template = path.join(__dirname, "yubikey-acct.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import device record:\n${body}`);
    await cas.doRequest("https://localhost:8443/cas/actuator/yubikeyAccountRepository/import", "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);
}

async function removeMultifactorTrustedRecord(record) {
    await cas.doRequest(`https://localhost:8443/cas/actuator/multifactorTrustedDevices/${record.recordKey}`);
}

async function verifyAccountManagementFlow(browser) {
    await removeWebAuthnDevices();
    const record = await importMultifactorTrustedRecord();

    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/account");

    await cas.goto(page, "https://localhost:8443/cas/account");
    await cas.sleep(1000);

    await cas.click(page, "#linkOverview");
    await cas.sleep(1000);

    await cas.click(page, "#linkAttributes");
    await cas.sleep(1000);

    await cas.click(page, "#linkApplications");
    await cas.sleep(1000);

    await cas.click(page, "#linkMfaRegisteredAccounts");
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:first-child", "Google Authenticator");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(2)", "1");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(3)", "MyRecordName");
    await cas.click(page, "button#register");
    await cas.sleep(2000);
    await cas.click(page, "#gauthRegistrationLink");
    await cas.sleep(2000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, "button#confirm");
    await cas.assertVisibility(page, "button#print");
    await cas.assertVisibility(page, "button#cancel");
    await cas.click(page, "button#cancel");
    await cas.sleep(2000);

    await cas.click(page, "#linkMfaTrustedDevices");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#divMultifactorTrustedDevices");

    await cas.click(page, "#linkSecurityQuestions");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#securityQuestionsTable");

    await cas.click(page, "#linkAuditLog");
    await cas.sleep(1000);

    await importWebAuthnDevice();

    await cas.click(page, "#linkPasswordManagement");
    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
    await cas.type(page, "#q0", "Salad");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");

    await removeWebAuthnDevices();
    await removeMultifactorTrustedRecord(record);
    
    await context.close();
}

async function verifyPasswordManagementFlow(browser) {
    await removeWebAuthnDevices();
    await removeAllYubiKeyDevices();
    const record = await importMultifactorTrustedRecord();

    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    
    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(2000);

    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.screenshot(page);

    await cas.assertInnerTextStartsWith(page, "#fm1 section div.alert p", "Your MFA provider has denied your attempt");

    await importWebAuthnDevice();
    await importYubiKeyDevice();
    
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

    await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");

    await removeWebAuthnDevices();
    await removeMultifactorTrustedRecord(record);
    await context.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    await importWebAuthnDevice();
    await importYubiKeyDevice();

    await verifyAccountManagementFlow(browser);
    await verifyPasswordManagementFlow(browser);

    await browser.close();
})();
