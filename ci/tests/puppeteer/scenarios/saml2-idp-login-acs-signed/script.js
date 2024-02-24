const puppeteer = require("puppeteer");
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-sp"));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");

    assert(response.ok());
    
    await cas.waitFor("https://localhost:9876/sp/saml/status", async () => {
        await cas.log("Trying without an exising SSO session...");
        await cas.goto(page, "https://localhost:9876/sp");
        await cas.waitForTimeout(page, 3000);
        await page.waitForSelector("#idpForm");
        await cas.submitForm(page, "#idpForm");
        await cas.waitForTimeout(page);
        await page.waitForSelector("#username");
        await cas.loginWith(page);
        await cas.waitForTimeout(page, 5000);
        await cas.logPage(page);
        await page.waitForSelector("body pre");
        let content = await cas.textContent(page, "body pre");
        let payload = JSON.parse(content);
        await cas.log(payload);
        assert(payload.form.SAMLResponse !== undefined);
        await cas.log("Trying with an exising SSO session...");
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.waitForTimeout(page, 4000);
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:9876/sp");
        await cas.waitForTimeout(page, 5000);
        await page.waitForSelector("#idpForm");
        await cas.submitForm(page, "#idpForm");
        await cas.waitForTimeout(page, 5000);
        await cas.logPage(page);
        content = await cas.textContent(page, "body pre");
        payload = JSON.parse(content);
        await cas.log(payload);
        assert(payload.form.SAMLResponse !== undefined);

        await browser.close();
        await cleanUp();
        process.exit();
    }, async (error) => {
        await cleanUp();
        await cas.log(error);
        throw error;
    });
})();

