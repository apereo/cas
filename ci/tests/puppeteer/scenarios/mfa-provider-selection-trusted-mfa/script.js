const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://example.com");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');
    await cas.click(page, "#btn-mfa-gauth");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.log("Fetching Scratch codes from /cas/actuator...");
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page);
    await cas.assertCookie(page, true, "MFATRUSTED");

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    let response = await cas.doRequest(baseUrl);
    let record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id !== null);
    assert(record.name !== null);
    await cas.goto(page, "https://localhost:8443/cas/logout");

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://example.com");
    await cas.loginWith(page);
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.log(`Removing trusted device ${record.name}`);
    await cas.doRequest(`${baseUrl}/${record.recordKey}`, "DELETE");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://example.com");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');
    
    await browser.close();
})();
