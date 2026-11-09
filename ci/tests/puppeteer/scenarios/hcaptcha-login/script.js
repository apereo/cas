const assert = require("assert");
const express = require("express");
const cas = require("../../cas.js");

async function setCaptchaResponse(page, value) {
    await page.evaluate((name, response) => {
        document.querySelectorAll(`[name="${name}"]`).forEach((el) => el.removeAttribute("name"));
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        input.value = response;
        document.getElementById("fm1").appendChild(input);
    }, "h-captcha-response", value);
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
        const token = new URLSearchParams(req.body).get("response");
        verifications.push(token);
        cas.log(`Received hCaptcha verification request for token ${token}`);
        res.json({success: token === "valid-hcaptcha-token"});
    });

    const server = app.listen(5611, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.assertVisibility(page, "#hcaptchaSection");

        await loginWithCaptcha(page, "invalid-hcaptcha-token");
        await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "reCAPTCHA’s validation failed.");
        await cas.assertCookie(page, false);

        await loginWithCaptcha(page, "valid-hcaptcha-token");
        await cas.assertCookie(page);
        assert.deepStrictEqual(verifications, ["invalid-hcaptcha-token", "valid-hcaptcha-token"]);

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
