const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");

(async () => {

    const template = path.join(__dirname, "device-record.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import device record:\n${body}`);
    await cas.doRequest("https://localhost:8443/cas/actuator/multifactorTrustedDevices/import", "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);
    
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.logPage(page);
    const url = await page.url();
    assert(url === "https://localhost:8443/cas/account");

    await cas.goto(page, "https://localhost:8443/cas/account");
    await cas.sleep(1000);
    
    await cas.click(page, "#linkOverview");
    await cas.sleep(1000);

    await cas.click(page, "#linkAttributes");
    await cas.sleep(1000);

    await cas.click(page, "#linkServices");
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

    await cas.click(page, "#linkPasswordManagement");
    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await browser.close();
})();
