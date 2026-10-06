const cas = require("../../cas.js");
const assert = require("assert");

const ENDPOINT = "https://localhost:8443/cas/actuator/gauthCredentialRepository";

/*
 * The export decodes what the repository stores, so with encryption on it shows whether the device
 * was written and read back through the cipher correctly: two of the five scratch codes are used up,
 * the secret is the plain base32 key rather than ciphertext, and the last login was recorded.
 */
function assertAccount(res, usedScratchCodes, authenticated) {
    assert(res.data.length === 1);
    const account = res.data[0];
    assert(account.scratchCodes.length === 5 - usedScratchCodes.length);
    assert(account.scratchCodes.every((code) => !usedScratchCodes.includes(String(code))));
    assert(/^[A-Z2-7]+=*$/.test(account.secretKey));
    if (authenticated) {
        assert(account.lastUsedDateTime !== undefined && account.lastUsedDateTime !== null);
    }
}

async function fetchAccounts(url, usedScratchCodes, authenticated = true) {
    await cas.doGet(url,
        (res) => assertAccount(res, usedScratchCodes, authenticated),
        (error) => {
            throw error;
        }, {
            "Content-Type": "application/json"
        });
}

async function loginWithToken(page, token) {
    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.type(page, "#token", token);
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} repository`);
    await cas.doRequest(ENDPOINT, "DELETE");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);

    const scratchCodes = await cas.innerTexts(page, "span[name='gauth-scratchcode']");
    await cas.log(`Scratch codes: ${scratchCodes}`);

    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#accountName");

    await cas.type(page, "#token", scratchCodes[0]);
    await cas.sleep(1000);
    await cas.click(page, "#registerButton");
    /*
     * The register button posts once over AJAX to have the token checked and then submits the
     * form for real, so the page that follows arrives after two round trips. Wait for the login
     * form, which only the token prompt carries, rather than for a fixed interval.
     */
    await cas.waitForElement(page, "#fm1");
    await fetchAccounts(`${ENDPOINT}/casuser`, [scratchCodes[0]], false);

    await cas.type(page, "#token", scratchCodes[1]);
    await cas.sleep(2000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.log(`Login panel: ${await cas.innerTexts(page, "#login")}`);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);

    const usedScratchCodes = [scratchCodes[0], scratchCodes[1]];
    await fetchAccounts(ENDPOINT, usedScratchCodes);
    await fetchAccounts(`${ENDPOINT}/casuser`, usedScratchCodes);

    for (const code of usedScratchCodes) {
        await cas.log(`Attempting to reuse scratch code ${code}`);
        await loginWithToken(page, code);
        await cas.assertCookie(page, false);
    }

    await loginWithToken(page, scratchCodes[2]);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await cas.gotoLoginWithAuthnMethod(page, undefined, "GoogleAuth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    for (let i = 0; i < 3; i++) {
        await cas.type(page, "#token", "657465");
        await cas.sleep(1000);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
    }
    await cas.type(page, "#token", "234231");
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#login div h2", "Blocked Multifactor Authentication Attempt");
    await cas.assertInnerTextStartsWith(page, "#login div p", "Your multifactor authentication attempt is blocked");

    await cas.doRequest(ENDPOINT, "DELETE");
    await cas.closeBrowser(browser);
})();
