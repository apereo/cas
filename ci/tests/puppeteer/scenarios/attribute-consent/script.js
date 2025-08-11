
const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");
const os = require("os");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);

    await cas.gotoLogin(page, "https://example.org");
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await cas.assertTextContent(page, "#appTitle", "The following attributes will be released to [https://example.org]:");
    await cas.assertTextContent(page, "#first-name", "first-name");
    await cas.assertTextContent(page, "#first-name-value", "[Apereo]");
    await cas.assertTextContent(page, "#last-name", "last-name");
    await cas.assertTextContent(page, "#last-name-value", "[CAS]");
    await cas.assertTextContent(page, "#email", "email");
    await cas.assertTextContent(page, "#email-value", "[casuser@example.org]");

    await cas.screenshot(page);
    await cas.click(page, "#optionsButton");
    await cas.sleep(2000);

    await cas.screenshot(page);
    let opt = await page.$("#optionAlways");
    assert(opt !== null);
    opt = await page.$("#optionAttributeName");
    assert(opt !== null);
    opt = await page.$("#optionAttributeValue");
    assert(opt !== null);
    await cas.assertTextContent(page, "#reminderTitle", "How often should I be reminded to consent again?");

    opt = await page.$("#reminder");
    assert(opt !== null);
    opt = await page.$("#reminderTimeUnit");
    assert(opt !== null);
    opt = await page.$("#cancel");
    assert(opt !== null);

    const confirm = await page.$("#confirm");
    assert(confirm !== null);
    await cas.click(page, "#confirm");
    await cas.waitForNavigation(page);
    await cas.assertTicketParameter(page);

    const baseUrl = "https://localhost:8443/cas/actuator/attributeConsent";
    const url = `${baseUrl}/casuser`;
    await cas.log(`Trying ${url}`);
    const response = await cas.goto(page, url);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    const template = path.join(__dirname, "consent-record.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import consent record:\n${body}`);
    await cas.doRequest(`${baseUrl}/import`, "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);

    await cas.doGet(`${baseUrl}/export`,
        async (res) => {
            const tempDir = os.tmpdir();
            const exported = path.join(tempDir, "consent.zip");
            res.data.pipe(fs.createWriteStream(exported));
            await cas.log(`Exported consent records are at ${exported}`);
        },
        async (error) => {
            throw error;
        }, {}, "stream");

    await cas.doDelete(`${baseUrl}/casuser/1`);
    await cas.doDelete(`${baseUrl}/casuser`);

    await cas.closeBrowser(browser);
})();

