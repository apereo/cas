const assert = require("assert");
const cas = require("../../cas.js");

async function verifyWebAuthnLogin(browser) {
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#webauthnLoginPanel div h2#status");
    await cas.assertTextContent(page, "#webauthnLoginPanel div h2#status","Login with FIDO2-enabled Device");

    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");

    const errorPanel = await page.$("#errorPanel");
    assert(await errorPanel === null);

    await cas.assertVisibility(page, "#messages");
    await cas.assertInvisibility(page, "#deviceTable");
    await cas.assertVisibility(page, "#authnButton");

    await page.on("response", (response) => {
        const url = response.url();
        cas.log(`URL: ${url}`);
        if (url.endsWith("webauthn/authenticate")) {
            assert(response.status() === 200);
        }
    });
    await cas.click(page, "#authnButton");
    await cas.sleep(1000);

    const baseEndpoint = "https://localhost:8443/cas/webauthn";
    const urls = [
        `${baseEndpoint}/authenticate`,
        `${baseEndpoint}/register`
    ];

    await urls.forEach((url) => {
        cas.log(`Evaluating URL ${url}`);
        cas.doPost(url, {}, {
            "Content-Type": "application/json"
        }, (res) => {
            throw (res);
        }, (error) => {
            assert(error.response.status === 403);
            assert(error.response.data.error === "Forbidden");
            assert(error.response.data.status === 403);
        });
    });

    await cas.log("Checking actuator endpoints...");
    const endpoints = ["health", "webAuthnDevices/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        const url = baseUrl + endpoints[i];
        const response = await cas.goto(page, url);
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.ok());
    }
}

async function verifyWebAuthnQRCode(browser) {
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");
    
    const qrCode = await cas.parseQRCode(page, "#imageQRCode");
    if (qrCode) {
        await cas.logb("Decoded QR Code:", qrCode.data);
        const page2 = await cas.newPage(browser);
        await cas.goto(page2, qrCode.data);
        await cas.sleep(1000);
        await cas.assertTextContent(page2, "#status", "Login with FIDO2-enabled Device");
        await cas.assertInvisibility(page2, "#imageQRCode");
    } else {
        throw "Failed to decode QR Code.";
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    let context = await browser.createBrowserContext();
    await verifyWebAuthnLogin(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifyWebAuthnQRCode(context);
    await context.close();

    await cas.closeBrowser(browser);
})();
