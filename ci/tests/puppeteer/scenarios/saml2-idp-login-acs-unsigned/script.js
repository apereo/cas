
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.waitFor("https://localhost:9876/sp/saml/status", async () => {
        await cas.log("Trying without an existing SSO session...");
        await cas.goto(page, "https://localhost:9876/sp");
        await cas.sleep(3000);
        await page.waitForSelector("#idpForm", {visible: true});
        await cas.submitForm(page, "#idpForm");
        await cas.sleep(9000);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

        await cas.log("Trying with an existing SSO session...");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:9876/sp");
        await cas.sleep(3000);
        await page.waitForSelector("#idpForm", {visible: true});
        await cas.submitForm(page, "#idpForm");
        await cas.sleep(9000);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

        await cas.closeBrowser(browser);
        await cleanUp();
    }, async (error) => {
        await cas.log(error);
        throw error;
    });
})();

