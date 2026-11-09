
const assert = require("assert");
const fs = require("fs");
const os = require("os");
const path = require("path");
const cas = require("../../cas.js");

const TOKEN_TIME_TO_KILL_SECONDS = 20;
const BUCKET_REFILL_SECONDS = 20;
const RATE_LIMITED_USERS = ["casuser", "casuser2", "casuser3"];

async function startLogin(page, user = "casuser") {
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple", "en");
    const response = await cas.loginWith(page, user, "Mellon");
    await cas.sleep(1000);
    return response;
}

async function resendToken(page) {
    await cas.assertVisibility(page, "#resendButton");
    const [response] = await Promise.all([
        page.waitForNavigation(),
        cas.click(page, "#resendButton")
    ]);
    await cas.sleep(1000);
    return response;
}

async function finishLogin(page, code) {
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    await cas.submitForm(page, "#registerform");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
}

async function loginWithDefaultToken(browser, page) {
    await startLogin(page);
    await cas.assertVisibility(page, "#token");
    const code = await cas.extractFromEmail(browser);
    assert.match(code, /^\d{6}$/);
    await finishLogin(page, code);
}

async function loginWithExpiredToken(browser, page) {
    await startLogin(page);
    const issuedBy = Date.now();
    await cas.assertVisibility(page, "#token");
    const expired = await cas.extractFromEmail(browser);
    assert.match(expired, /^[A-Za-z0-9]{10}$/);

    await cas.log(`Waiting for token ${expired} to outlive its ${TOKEN_TIME_TO_KILL_SECONDS} seconds`);
    await cas.sleep(Math.max(0, issuedBy + (TOKEN_TIME_TO_KILL_SECONDS + 3) * 1000 - Date.now()));
    await page.bringToFront();
    await cas.type(page, "#token", expired);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertTextContentStartsWith(page, "div .banner-danger p", "Multifactor authentication attempt has failed");

    await cas.log("Resending replaces the expired token with a new one");
    await resendToken(page);
    const code = await cas.extractFromEmail(browser);
    assert.match(code, /^[A-Za-z0-9]{10}$/);
    assert.notEqual(code, expired);
    await finishLogin(page, code);
}

/**
 * Picks a user whose bucket no earlier attempt has drained: a retried attempt runs against the same server, and
 * buckets are kept per user and refill at intervals counted from their creation.
 *
 * @returns the user to log in with
 */
function nextRateLimitedUser() {
    const marker = path.join(os.tmpdir(), `${process.env.SCENARIO}-rate-limited-attempts`);
    let attempt = 0;
    if (fs.existsSync(marker) && Date.now() - fs.statSync(marker).mtimeMs < 10 * 60 * 1000) {
        attempt = Number(fs.readFileSync(marker, "utf8")) + 1;
    }
    fs.writeFileSync(marker, String(attempt));
    return RATE_LIMITED_USERS[attempt % RATE_LIMITED_USERS.length];
}

async function loginWithRateLimits(browser, page) {
    const user = nextRateLimitedUser();
    await cas.log(`Sending and validating tokens for ${user} draw from one bucket of 2`);
    let response = await startLogin(page, user);
    const bucketCreated = Date.now();
    await cas.assertVisibility(page, "#token");
    assert.equal(response.headers()["x-rate-limit-remaining"], "1");

    response = await resendToken(page);
    await cas.assertVisibility(page, "#token");
    assert.equal(response.headers()["x-rate-limit-remaining"], "0");

    await cas.log("A third token within the refill period is refused");
    response = await resendToken(page);
    assert(response.headers()["x-rate-limit-retry-after-seconds"] !== undefined);
    await cas.assertInnerText(page, "#content h2", "MFA Provider Unavailable");
    await cas.assertInvisibility(page, "#token");

    await cas.log("Once the bucket refills, a new token is sent and accepted");
    await cas.sleep(Math.max(0, bucketCreated + (BUCKET_REFILL_SECONDS + 3) * 1000 - Date.now()));
    response = await startLogin(page, user);
    await cas.assertVisibility(page, "#token");
    assert.equal(response.headers()["x-rate-limit-remaining"], "1");
    const code = await cas.extractFromEmail(browser);
    await finishLogin(page, code);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const variation = process.env.SCENARIO_VARIATION;
    await cas.log(`Running variation ${variation}`);
    if (variation === "token-policy") {
        await loginWithExpiredToken(browser, page);
    } else if (variation === "rate-limited") {
        await loginWithRateLimits(browser, page);
    } else {
        await loginWithDefaultToken(browser, page);
    }
    await cas.closeBrowser(browser);
})();
