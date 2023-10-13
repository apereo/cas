const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);

    await cas.logPage(page);
    let url = await page.url();
    assert(url === "https://localhost:8443/cas/account");

    await cas.goto(page, "https://localhost:8443/cas/account");
    await page.waitForTimeout(1000);

    await cas.click(page, "#linkOverview");
    await page.waitForTimeout(1000);

    await cas.click(page, "#linkAttributes");
    await page.waitForTimeout(1000);

    await cas.click(page, "#linkServices");
    await page.waitForTimeout(1000);

    await cas.click(page, "#linkMfaRegisteredAccounts");
    await page.waitForTimeout(1000);

    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:first-child", "Google Authenticator");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(2)", "1");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(3)", "MyRecordName");
    await cas.click(page, "button#register");
    await page.waitForTimeout(2000);
    await cas.click(page, "#gauthRegistrationLink");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, "button#confirm");
    await cas.assertVisibility(page, "button#print");
    await cas.assertVisibility(page, "button#cancel");
    await cas.click(page, "button#cancel");
    await page.waitForTimeout(2000);

    await cas.click(page, "#linkSecurityQuestions");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, "#securityQuestionsTable");

    await cas.click(page, "#linkAuditLog");
    await page.waitForTimeout(1000);

    await cas.click(page, "#linkPasswordManagement");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page, false);

    await browser.close();
})();
