const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

/**
 * Logs in again and submits a Google Authenticator code.
 *
 * @param page the page
 * @param token the code
 */
async function loginWithToken(page, token) {
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth", "en");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.type(page, "#token", token);
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
}

/**
 * A code that was already used is refused even while it is still current, and a code of the next time step is
 * accepted only when the window allows it: the default window of three steps does, the window of one step in the
 * custom-codes variation does not.
 *
 * @param page the page, with a session from logging in with the code
 * @param otpConfig the parsed registration URL
 * @param usedOtp the code used to log in
 */
async function verifyReplayAndWindow(page, otpConfig, usedOtp) {
    await cas.gotoLogout(page);
    await cas.logg(`Replaying the code ${usedOtp}`);
    await loginWithToken(page, usedOtp);
    await cas.assertCookie(page, false);

    const nextStep = await cas.generateOtp(otpConfig, 1);
    await loginWithToken(page, nextStep);
    if (process.env.SCENARIO_VARIATION === "custom-codes") {
        await cas.logg("The code of the next time step is outside a window of one step");
        await cas.assertCookie(page, false);
    } else {
        await cas.logg("The code of the next time step is inside the default window");
        await cas.assertCookie(page);
        await cas.gotoLogout(page);
    }
}

(async () => {

    await cas.doRequest("https://localhost:8443/cas/actuator/gauthCredentialRepository", "DELETE");
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth", "en");
    await cas.loginWith(page);

    await cas.assertInnerTextStartsWith(page, "#login h2", "Your account is not registered");
    await cas.assertVisibility(page, "img#imageQRCode");
    await cas.assertVisibility(page, "#seckeypanel pre");
    await cas.assertVisibility(page, "#scratchcodes");
    assert((await page.$$("#scratchcodes div.mdc-chip")).length === 5);
    if (process.env.SCENARIO_VARIATION === "custom-codes") {
        const registration = await cas.parseOtpAuthenticationUrl((await cas.parseQRCode(page, "#imageQRCode")).data);
        assert.equal(registration.digits, 8, "The registration URL must carry the number of digits");
        assert.equal(registration.period, 60, "The registration URL must carry the period");
    }

    const qrCode = await cas.parseQRCode(page, "#imageQRCode");
    await cas.logg(`QR code is ${qrCode.data}`);
    await cas.sleep(2000);

    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#accountName");
    await cas.assertAccessibility(page, "Google Authenticator registration confirmation");

    const otpConfig = await cas.parseOtpAuthenticationUrl(qrCode.data);
    await cas.logg(otpConfig);

    let otp = await cas.generateOtp(otpConfig);
    await cas.logg(`Generated registration OTP: ${otp}`);

    await cas.type(page, "#token", otp);
    await cas.sleep(2000);
    await cas.click(page, "#registerButton");
    await cas.sleep(2000);

    otp = await cas.generateOtp(otpConfig);
    await cas.logg(`Generated authentication OTP: ${otp}`);
    await cas.type(page, "#token", otp);
    await cas.sleep(2000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(2000);
    await cas.assertCookie(page);

    await verifyReplayAndWindow(page, otpConfig, otp);

    await cas.logb("Attempting to authenticate via REST...");
    otp = await cas.generateOtp(otpConfig);
    const formData = {
        username: "casuser",
        password: "Mellon",
        gauthotp: otp
    };
    const postData = querystring.stringify(formData);
    const restResult = JSON.parse(await cas.doRequest("https://localhost:8443/cas/v1/users",
        "POST",
        {
            "Accept": "application/json",
            "Content-Length": Buffer.byteLength(postData),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        200,
        postData));
    await cas.log(restResult);
    assert(restResult.authentication.principal.id === "casuser");
    
    await cas.closeBrowser(browser);
})();
