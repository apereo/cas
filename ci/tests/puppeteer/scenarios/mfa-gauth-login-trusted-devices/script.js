const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const os = require("os");
const path = require("path");

const TRUSTED_DEVICES = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
const TRUSTED_SERVICE = "https://localhost:9859/anything/trusted";
const SENSITIVE_SERVICE = "https://localhost:9859/anything/sensitive";
const TRUSTED_DEVICE_NAME = "My Trusted Device";
const EXPIRING_DEVICE_NAME = "Expiring Device";
const EXPIRING_DEVICE_SECONDS = 30;

/*
 * The device fingerprint is "<client-ip>@<user-agent>": the cookie component is turned off, so a fresh browser
 * context with the same user agent is the same device, while another user agent or client address is not.
 * Records are kept in a JSON file, which lets the script see what the cleaner removed without asking CAS,
 * since every read through CAS removes expired records first.
 */

async function withPage(browser, options, callback) {
    const context = await browser.createBrowserContext();
    try {
        const page = await cas.newPage(context);
        if (options.userAgent !== undefined) {
            await page.setUserAgent({userAgent: options.userAgent});
        }
        if (options.headers !== undefined) {
            await page.setExtraHTTPHeaders(options.headers);
        }
        return await callback(page);
    } finally {
        await context.close();
    }
}

async function fetchTrustedDevices() {
    const response = await cas.doRequest(`${TRUSTED_DEVICES}/casuser`, "GET", {"Accept": "application/json"});
    return JSON.parse(response);
}

async function removeAllTrustedDevices() {
    const records = JSON.parse(await cas.doRequest(TRUSTED_DEVICES, "GET", {"Accept": "application/json"}));
    for (const record of records) {
        await cas.log(`Removing trusted device ${record.name} left by an earlier attempt`);
        await cas.doRequest(`${TRUSTED_DEVICES}/${encodeURIComponent(record.recordKey)}`, "DELETE");
    }
    const remaining = JSON.parse(await cas.doRequest(TRUSTED_DEVICES, "GET", {"Accept": "application/json"}));
    assert.equal(remaining.length, 0);
}

async function storedTrustedDeviceNames() {
    const file = path.join(os.tmpdir(), `${process.env.SCENARIO}-trusted-devices.json`);
    for (let attempt = 1; attempt <= 3; attempt++) {
        try {
            const names = [];
            const collect = (node) => {
                if (Array.isArray(node)) {
                    node.forEach(collect);
                } else if (node !== null && typeof node === "object") {
                    if (typeof node.principal === "string" && typeof node.name === "string") {
                        names.push(node.name);
                    } else {
                        Object.values(node).forEach(collect);
                    }
                }
            };
            collect(JSON.parse(fs.readFileSync(file, "utf8")));
            await cas.log(`Trusted devices stored in ${file}: ${names}`);
            return names;
        } catch (e) {
            await cas.logr(`Unable to read ${file}: ${e}`);
            await cas.sleep(1000);
        }
    }
    throw `Unable to read trusted devices from ${file}`;
}

function toMillis(date) {
    return typeof date === "number" ? date : Date.parse(String(date));
}

async function assertTrustedDeviceSkipsMfa(page) {
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInvisibility(page, "#token");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

async function assertMfaRequired(page) {
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#token");
}

async function loginAndRegisterTrustedDevice(browser, deviceName, options = {}) {
    return withPage(browser, options, async (page) => {
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.log("Using scratch code to login...");

        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page,"#token", scratch);

        await cas.submitForm(page, "#fm1");
        await cas.innerText(page, "#deviceName");
        await cas.type(page, "#deviceName", deviceName);
        await cas.sleep(1000);
        await cas.assertInvisibility(page, "#expiration");
        await cas.assertVisibility(page, "#timeUnit");
        if (options.expirationSeconds !== undefined) {
            await page.select("#timeUnit", "SECONDS");
            await cas.type(page, "#expiration", String(options.expirationSeconds));
        }
        await cas.submitForm(page, "#registerform");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        return page.evaluate(() => navigator.userAgent);
    });
}

async function verifyDeviceFingerprint(userAgent) {
    const records = await fetchTrustedDevices();
    console.dir(records, {depth: null, colors: true});
    assert.equal(records.length, 1);
    assert.equal(records[0].name, TRUSTED_DEVICE_NAME);
    const fingerprint = records[0].deviceFingerprint;
    assert(fingerprint.endsWith(`@${userAgent}`), `Fingerprint ${fingerprint} must end with the user agent`);
    const clientIp = fingerprint.substring(0, fingerprint.length - userAgent.length - 1);
    assert(clientIp.length > 0 && !clientIp.includes("@"), `Fingerprint ${fingerprint} must start with the client address`);
}

async function verifyFingerprintMismatch(browser, userAgent) {
    await cas.log("A fresh browser with the same address and user agent is the trusted device, without any cookie");
    await withPage(browser, {}, assertTrustedDeviceSkipsMfa);

    await cas.log("Another user agent is another device and must complete multifactor authentication");
    await withPage(browser, {userAgent: `${userAgent} CasOtherDevice`}, assertMfaRequired);

    await cas.log("Another client address is another device and must complete multifactor authentication");
    await withPage(browser, {headers: {"X-Forwarded-For": "203.0.113.7"}}, assertMfaRequired);
}

async function verifyServiceBypassesTrustedDevice(browser) {
    await cas.log("The trusted device skips multifactor authentication for an ordinary application");
    await withPage(browser, {}, async (page) => {
        await cas.gotoLoginWithAuthnMethod(page, TRUSTED_SERVICE, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(2000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(TRUSTED_SERVICE, ticket);
        assert(json.serviceResponse.authenticationSuccess.user.includes("casuser"));
    });

    await cas.log("An application whose policy bypasses trusted devices asks for the token on the trusted device");
    await withPage(browser, {}, async (page) => {
        await cas.gotoLogin(page, SENSITIVE_SERVICE);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertVisibility(page, "#token");
        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page, "#token", scratch);
        await cas.submitForm(page, "#fm1");
        await cas.sleep(2000);
        await cas.assertInvisibility(page, "#deviceName");
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(SENSITIVE_SERVICE, ticket);
        assert(json.serviceResponse.authenticationSuccess.user.includes("casuser"));
    });
    const records = await fetchTrustedDevices();
    assert.equal(records.length, 1, "No device may be registered for an application that bypasses trusted devices");
}

async function loginFromPublicWorkstation(browser) {
    const context = await browser.createBrowserContext();
    const service = TRUSTED_SERVICE;

    try {
        const page = await cas.newPage(context);
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-gauth");
        await cas.assertVisibility(page, "#publicWorkstationButton");
        await cas.click(page, "#publicWorkstationButton");
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.log("Using scratch code to login...");
        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page,"#token", scratch);
        await cas.submitForm(page, "#fm1");
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page, false);

        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user.includes("casuser"));

    } finally {
        await context.close();
    }
}

async function verifyExpiredTrustedDevice(browser, userAgent) {
    const expiringUserAgent = `${userAgent} CasExpiringDevice`;
    await cas.log(`Registering a device trusted for ${EXPIRING_DEVICE_SECONDS} seconds`);
    await loginAndRegisterTrustedDevice(browser, EXPIRING_DEVICE_NAME,
        {userAgent: expiringUserAgent, expirationSeconds: EXPIRING_DEVICE_SECONDS});
    await withPage(browser, {userAgent: expiringUserAgent}, assertTrustedDeviceSkipsMfa);

    const record = (await fetchTrustedDevices()).find((entry) => entry.name === EXPIRING_DEVICE_NAME);
    assert(record !== undefined, "The expiring device must be registered");
    const expiresAt = toMillis(record.expirationDate);
    assert(!Number.isNaN(expiresAt), `Unable to read the expiration date ${record.expirationDate}`);
    await cas.log(`Expiring device record expires at ${new Date(expiresAt).toISOString()}`);
    assert(expiresAt - Date.now() <= EXPIRING_DEVICE_SECONDS * 1000, "The device must expire after the chosen duration");
    assert((await storedTrustedDeviceNames()).includes(EXPIRING_DEVICE_NAME));

    await cas.log("Waiting for the device to expire and for the cleaner to run");
    await cas.sleep(Math.max(0, expiresAt - Date.now()) + 12000);
    const stored = await storedTrustedDeviceNames();
    assert(!stored.includes(EXPIRING_DEVICE_NAME), "The cleaner must remove the expired device");
    assert(stored.includes(TRUSTED_DEVICE_NAME), "The cleaner must keep devices that have not expired");

    await cas.log("The expired device must complete multifactor authentication again");
    await withPage(browser, {userAgent: expiringUserAgent}, assertMfaRequired);
    const records = await fetchTrustedDevices();
    assert(records.every((entry) => entry.name !== EXPIRING_DEVICE_NAME));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await removeAllTrustedDevices();
    const userAgent = await loginAndRegisterTrustedDevice(browser, TRUSTED_DEVICE_NAME);
    await verifyDeviceFingerprint(userAgent);
    await verifyFingerprintMismatch(browser, userAgent);
    await verifyServiceBypassesTrustedDevice(browser);
    await loginFromPublicWorkstation(browser);
    await verifyExpiredTrustedDevice(browser, userAgent);
    await cas.closeBrowser(browser);
})();
