
const cas = require("../../cas.js");

(async () => {
    let failed = false;
    try {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);

        await cas.log("Trying without an exising SSO session...");
        await cas.goto(page, "https://localhost:9876/fediz");
        await cas.sleep(5000);
        await cas.screenshot(page);
        await page.waitForSelector("#logincas", {visible: true});
        await cas.click(page, "#logincas");
        await cas.sleep(3000);
        await cas.screenshot(page);
        await page.waitForSelector("#username", {visible: true});
        await cas.loginWith(page);
        await page.waitForResponse((response) => response.status() === 200);
        await cas.sleep(3000);
        await cas.screenshot(page);
        await cas.logPage(page);
        await cas.assertInnerText(page, "#principalId", "casuser");
        await cas.assertVisibility(page, "#assertion");
        await cas.sleep(2000);
        await cas.assertInnerText(page, "#claim0", "http://schemas.xmlsoap.org/claims/EmailAddress:casuser@example.org");
        await cas.assertInnerText(page, "#claim1", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname:casuser");
        await cas.assertInnerText(page, "#claim2", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress:CAS@example.org");

        await cas.log("Trying with an exising SSO session...");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:9876/fediz");
        await cas.sleep(2000);
        await page.waitForSelector("#logincas", {visible: true});
        await cas.click(page, "#logincas");
        await cas.sleep(2000);
        await cas.logPage(page);
        await cas.assertInnerText(page, "#principalId", "casuser");
        await cas.assertVisibility(page, "#assertion");
        await cas.sleep(2000);
        await cas.assertInnerText(page, "#claim0", "http://schemas.xmlsoap.org/claims/EmailAddress:casuser@example.org");
        await cas.assertInnerText(page, "#claim1", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname:casuser");
        await cas.assertInnerText(page, "#claim2", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress:CAS@example.org");

        await browser.close();
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        if (!failed) {
            await process.exit(0);
        }
    }
})();

