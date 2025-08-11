
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.log("Cleanup done");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.waitFor("https://localhost:9876/sp/saml/status", async () => {
        try {
            await cas.goto(page, "https://localhost:9876/sp");
            await cas.sleep(3000);
            await page.waitForSelector("#idpForm", {visible: true});
            await cas.submitForm(page, "#idpForm");
            await cas.sleep(3000);
            await page.waitForSelector("#username", {visible: true});

            await cas.loginWith(page, "duobypass", "Mellon");
            await cas.sleep(3000);

            await cas.log("Checking for page URL...");
            await cas.logPage(page);
            await cas.sleep(3000);
            await cas.assertInnerText(page, "#principal", "casuser@example.org");
            await cas.assertInnerText(page, "#authnContextClass", "https://refeds.org/profile/mfa");
            await cleanUp();
        } finally {
            await cas.closeBrowser(browser);
        }
    }, async (error) => {
        await cas.log(error);
        throw error;
    });
})();

