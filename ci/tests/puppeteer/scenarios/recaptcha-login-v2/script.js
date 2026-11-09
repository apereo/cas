const assert = require("assert");
const express = require("express");
const cas = require("../../cas.js");

const CAPTCHA_FIELD = "g-recaptcha-response";

async function setCaptchaResponse(page, value) {
    await page.evaluate((name, response) => {
        document.querySelectorAll(`[name="${name}"]`).forEach((el) => el.removeAttribute("name"));
        if (response !== null) {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = name;
            input.value = response;
            document.getElementById("fm1").appendChild(input);
        }
    }, CAPTCHA_FIELD, value);
}

async function assertCaptchaFailure(page) {
    await cas.assertVisibility(page, "#loginErrorsPanel");
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "reCAPTCHA’s validation failed.");
    await cas.assertCookie(page, false);
}

async function loginWithCaptcha(page, value) {
    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#password", "Mellon", true);
    await setCaptchaResponse(page, value);
    await Promise.all([page.waitForNavigation(), cas.pressEnter(page)]);
}

(async () => {
    const verifications = [];
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.post("/siteverify", (req, res) => {
        const params = new URLSearchParams(req.body);
        const token = params.get("response");
        verifications.push({secret: params.get("secret"), response: token});
        cas.log(`Received captcha verification request for token ${token}`);
        res.json({success: token === "valid-captcha-token"});
    });

    const server = app.listen(5611, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);

        if (process.env.SCENARIO_VARIATION === "ip-pattern-unmatched") {
            await cas.log("Captcha is only activated for 192.0.2.x clients; it must not be required here");
            await cas.assertInvisibility(page, "#recaptchaV2Section");
            await cas.assertInvisibility(page, "#g-recaptcha");
            await loginWithCaptcha(page, null);
            await cas.assertCookie(page);
            assert.equal(verifications.length, 0);
        } else {
            await cas.assertVisibility(page, "#recaptchaV2Section");
            await cas.assertVisibility(page, "#g-recaptcha");

            await cas.log("Submitting the login form without a captcha response");
            await loginWithCaptcha(page, null);
            await assertCaptchaFailure(page);
            assert.equal(verifications.length, 0);

            await cas.log("Submitting the login form with a captcha response the verifier rejects");
            await loginWithCaptcha(page, "invalid-captcha-token");
            await assertCaptchaFailure(page);
            assert.equal(verifications.length, 1);
            assert.equal(verifications[0].response, "invalid-captcha-token");
            assert.equal(verifications[0].secret, "7Let09ISAAAAAIwdE2YXEQ3k4BBRmfGabOQpA3ka");

            await cas.log("Submitting the login form with a captcha response the verifier accepts");
            await loginWithCaptcha(page, "valid-captcha-token");
            await cas.assertCookie(page);
            await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");
            assert.equal(verifications.length, 2);
            assert.equal(verifications[1].response, "valid-captcha-token");
        }

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
