
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-sp"));
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
        await cas.sleep(2000);

        await page.waitForSelector("#username", {visible: true});
        await cas.loginWith(page);
        await page.waitForResponse((response) => response.status() === 200);
        await cas.sleep(3000);
        await cas.logPage(page);
        await page.waitForSelector("body pre", { visible: true });
        let content = await cas.textContent(page, "body pre");
        let payload = JSON.parse(content);
        await cas.log(payload);
        assert(payload.form.SAMLResponse !== undefined);
        await cas.log("Trying with an existing SSO session...");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.sleep(6000);
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:9876/sp");
        await cas.sleep(3000);
        await page.waitForSelector("#idpForm", {visible: true});
        await cas.submitForm(page, "#idpForm");
        await cas.sleep(3000);
        await cas.logPage(page);
        await page.waitForSelector("body pre", { visible: true });
        content = await cas.textContent(page, "body pre");
        payload = JSON.parse(content);
        await cas.log(payload);
        assert(payload.form.SAMLResponse !== undefined);

        await cas.closeBrowser(browser);
        await cleanUp();
        process.exit();
    }, async (error) => {
        await cas.log(error);
        throw error;
    });
})();

