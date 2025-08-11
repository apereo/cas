const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

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

    const qrCode = await cas.parseQRCode(page, "#imageQRCode");
    await cas.logg(`QR code is ${qrCode.data}`);
    await cas.sleep(2000);

    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#accountName");

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
