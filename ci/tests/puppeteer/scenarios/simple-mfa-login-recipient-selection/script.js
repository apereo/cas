
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, "#continueButton");
    await cas.log("Checking all emails");
    await page.evaluate(() => {
        const emails = document.querySelectorAll("input[type=checkbox]");
        emails.forEach((lnk) => lnk.click());
    });
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");

    await cas.sleep(5000);
    await cas.assertVisibility(page, "#token");
    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.sleep(2000);
    await cas.click(page2, "table tbody td a");
    await cas.sleep(2000);
    const addresses = await cas.textContent(page2, "div[name=addresses] span");
    assert(addresses.includes("casperson@example.com"));
    assert(addresses.includes("casuser@example.org"));
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await cas.log(`Code to use is extracted as ${code}`);
    await page2.close();
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(4000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(2000);
    await cas.assertCookie(page);
    await browser.close();
})();
