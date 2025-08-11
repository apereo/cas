
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

async function passwordResetFlowWithoutTrustedDevice(browser) {
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

    const link = await cas.extractFromEmail(context);
    assert(link !== undefined);
    await cas.goto(page, link);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
    await cas.type(page, "#q0", "Salad");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);

    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page, "#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.attributeValue(page, ".generate-password", "title", "Generate password");
    await context.close();
}

async function importTrustedDevice() {
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

async function removeTrustedDevice(record) {
    await cas.doDelete(`https://localhost:8443/cas/actuator/multifactorTrustedDevices/${record.recordKey}`);
}

async function passwordResetFlowWithTrustedDevice(browser) {
    const record = await importTrustedDevice();
    const context = await browser.createBrowserContext();
    try {
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

        const link = await cas.extractFromEmail(context);
        assert(link !== undefined);
        await cas.goto(page, link);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
        await cas.type(page, "#q0", "Salad");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.log(`Using scratch code ${scratch} to login...`);
        await cas.type(page, "#token", scratch);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        
        await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
        await cas.attributeValue(page, ".generate-password", "title", "Generate password");
    } finally {
        await removeTrustedDevice(record);
        await context.close();
    }
}

async function passwordResetFlowWithAccountProfileWithoutTrustedDevice(browser) {
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
    await cas.click(page, "#linkPasswordManagement");
    await cas.sleep(10000);
    await cas.assertCookie(page, false);
    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
    await cas.type(page, "#q0", "Salad");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page, "#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.attributeValue(page, ".generate-password", "title", "Generate password");
}

async function passwordResetFlowWithAccountProfileWithTrustedDeviceIgnored(browser) {
    const record = await importTrustedDevice();
    const context = await browser.createBrowserContext();
    try {
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
        await cas.click(page, "#linkPasswordManagement");
        await cas.sleep(10000);
        await cas.assertCookie(page, false);
        await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
        await cas.type(page, "#q0", "Salad");
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.log(`Using scratch code ${scratch} to login...`);
        await cas.type(page, "#token", scratch);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
        await cas.attributeValue(page, ".generate-password", "title", "Generate password");
    } finally {
        await removeTrustedDevice(record);
        await context.close();
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await passwordResetFlowWithoutTrustedDevice(browser);
    await passwordResetFlowWithTrustedDevice(browser);
    await passwordResetFlowWithAccountProfileWithoutTrustedDevice(browser);
    await passwordResetFlowWithAccountProfileWithTrustedDeviceIgnored(browser);
    
    await cas.closeBrowser(browser);
})();
