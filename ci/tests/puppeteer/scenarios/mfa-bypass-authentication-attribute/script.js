
const cas = require("../../cas.js");

const SERVICE = "https://localhost:9859/anything/cas";

/**
 * Logs in and checks that the bypass rule of the variation skipped the YubiKey step.
 *
 * @param page the page
 */
async function verifyBypassed(page) {
    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    if (process.env.SCENARIO_VARIATION === "request-header") {
        await cas.logg("Without the header, YubiKey is required");
        const context = await browser.createBrowserContext();
        const plain = await cas.newPage(context);
        await cas.gotoLogin(plain, SERVICE);
        await cas.loginWith(plain);
        await cas.sleep(1000);
        await cas.assertTextContent(plain, "#login h3", "Use your registered YubiKey device(s) to authenticate.");
        await context.close();

        await cas.logg("With the header, YubiKey is bypassed");
        const page = await cas.newPage(browser);
        await page.setExtraHTTPHeaders({"X-Bypass-MFA": "true"});
        await verifyBypassed(page);
    } else {
        await verifyBypassed(await cas.newPage(browser));
    }
    await cas.closeBrowser(browser);
})();
