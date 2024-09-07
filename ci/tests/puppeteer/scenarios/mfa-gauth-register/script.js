const assert = require("assert");
const cas = require("../../cas.js");
const qrCode = require("qrcode-reader");
const fs = require("fs");
const jimp = require("jimp");

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

    const src = await page.$eval("#imageQRCode", (element) => element.getAttribute("src"));
    const data = src.replace(/^data:image\/jpeg;base64,/, "");

    await cas.removeDirectoryOrFile(`${__dirname}/out.ignore`);
    await fs.writeFileSync(`${__dirname}/out.ignore`, data, "base64");
    const buffer = fs.readFileSync(`${__dirname}/out.ignore`);

    await jimp.read(buffer, async (err, image) => {
        if (err) {
            await cas.logr(err);
            throw err;
        }
        const qrcode = new qrCode();
        qrcode.callback = async (err, payload) => {
            if (err) {
                await cas.logr(err);
                throw err;
            }
            await cas.logg(`QR code is ${payload.result}`);

            const confirm = await page.$("#confirm");
            await confirm.click();
            await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
            await cas.assertVisibility(page, "#token");
            await cas.assertVisibility(page, "#accountName");

            const otpConfig = await cas.parseOtpAuthenticationUrl(payload.result);
            await cas.logg(otpConfig);

            const otp1 = await cas.generateOtp(otpConfig);
            await cas.logg(`Generated registration OTP: ${otp1}`);

            await cas.type(page, "#token", otp1);
            await cas.sleep(2000);
            await cas.click(page, "#registerButton");
            await cas.sleep(2000);

            let otp2 = await cas.generateOtp(otpConfig);
            while (otp2 === otp1) {
                await cas.logb(`Generated OTP matches the previous value: ${otp2}. Trying again...`);
                await cas.sleep(3000);
                otp2 = await cas.generateOtp(otpConfig);
            }
            await cas.logg(`Generated authentication OTP: ${otp2}`);
            await cas.type(page, "#token", otp2);
            await cas.sleep(2000);
            await cas.pressEnter(page);
            await cas.waitForNavigation(page);
            await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
            await cas.assertInnerText(page, "#content div h2", "Log In Successful");
            await cas.sleep(2000);
            await cas.assertCookie(page);

            await browser.close();
            await process.exit(0);
        };
        qrcode.decode(image.bitmap);
    });
})();
