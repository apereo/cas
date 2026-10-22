
const path = require("path");
const zlib = require("zlib");
const cas = require("../../cas.js");
const assert = require("assert");

const TAMPERED_ACS = "https://localhost:9859/anything/tampered-acs";

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-sp"));
}

function tamperAssertionConsumerService(xml) {
    return xml.replace(/AssertionConsumerServiceURL="[^"]+"/, `AssertionConsumerServiceURL="${TAMPERED_ACS}"`);
}

async function submitTamperedAuthnRequest(browser) {
    const page = await cas.newPage(browser, true);
    let handled = false;
    let tampered = false;
    let tamperedRedirectUrl = undefined;
    await page.setRequestInterception(true);
    page.on("request", (request) => {
        const url = new URL(request.url());
        const post = request.method() === "POST";
        const form = new URLSearchParams(post ? request.postData() ?? "" : url.search);
        const samlRequest = form.get("SAMLRequest");
        if (handled || samlRequest === null || !url.pathname.startsWith("/cas/idp/profile/SAML2/") || !url.pathname.endsWith("/SSO")) {
            request.continue();
            return;
        }
        handled = true;
        const encoded = Buffer.from(samlRequest, "base64");
        const xml = post ? encoded.toString("utf8") : zlib.inflateRawSync(encoded).toString("utf8");
        const altered = tamperAssertionConsumerService(xml);
        tampered = altered !== xml;
        cas.log(`Tampered with ${request.method()} authentication request: ${tampered}`);
        if (post) {
            form.set("SAMLRequest", Buffer.from(altered, "utf8").toString("base64"));
            request.continue({postData: form.toString()});
        } else {
            url.searchParams.set("SAMLRequest", zlib.deflateRawSync(Buffer.from(altered, "utf8")).toString("base64"));
            tamperedRedirectUrl = url.toString();
            request.abort();
        }
    });
    await cas.goto(page, "https://localhost:9876/sp");
    await cas.sleep(3000);
    await page.waitForSelector("#idpForm", {visible: true});
    await cas.submitForm(page, "#idpForm");
    await cas.sleep(4000);
    if (tamperedRedirectUrl !== undefined) {
        await cas.goto(page, tamperedRedirectUrl);
        await cas.sleep(2000);
    }
    await cas.logPage(page);
    assert(tampered, "The signed authentication request must carry an AssertionConsumerServiceURL to tamper with");
    assert(!page.url().startsWith(TAMPERED_ACS), "No response may be delivered to the tampered ACS");
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await page.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.waitFor("https://localhost:9876/sp/saml/status", async () => {
        await cas.log("Trying with a signed authentication request whose ACS is altered in transit...");
        await submitTamperedAuthnRequest(browser);

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

