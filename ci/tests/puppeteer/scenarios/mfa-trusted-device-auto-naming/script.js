const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    console.log("Fetching Scratch codes from /cas/actuator...");
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();

    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    
    console.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.assertTicketGrantingCookie(page);
    let response = await cas.doRequest("https://localhost:8443/cas/actuator/multifactorTrustedDevices");
    let record = JSON.parse(response)[0];
    assert(record.id !== null);
    assert(record.name !== null);
    await browser.close();
})();
