const assert = require("assert");
const express = require("express");
const cas = require("../../cas.js");

const CAPTCHA_FIELD = "g-recaptcha-token";

const SCORES = {
    "low-score-token": 0.3,
    "threshold-score-token": 0.7,
    "high-score-token": 0.9
};

async function setCaptchaToken(page, value) {
    await page.evaluate((name, token) => {
        document.querySelectorAll(`[name="${name}"]`).forEach((el) => el.removeAttribute("name"));
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        input.value = token;
        document.getElementById("fm1").appendChild(input);
    }, CAPTCHA_FIELD, value);
}

async function loginWithToken(page, token) {
    await cas.log(`Submitting the login form with captcha token ${token}, scored ${SCORES[token]}`);
    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#password", "Mellon", true);
    await setCaptchaToken(page, token);
    await Promise.all([page.waitForNavigation(), cas.pressEnter(page)]);
}

(async () => {
    const verifications = [];
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.post("/siteverify", (req, res) => {
        const token = new URLSearchParams(req.body).get("response");
        verifications.push(token);
        cas.log(`Received captcha verification request for token ${token}`);
        res.json({success: true, score: SCORES[token], action: "login"});
    });

    const server = app.listen(5611, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.assertVisibility(page, "#recaptchaV3Section");
        const grecaptcha = await page.$("#g-recaptcha-token");
        assert(await grecaptcha !== undefined);

        for (const token of ["low-score-token", "threshold-score-token"]) {
            await loginWithToken(page, token);
            await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "reCAPTCHA’s validation failed.");
            await cas.assertCookie(page, false);
        }

        await loginWithToken(page, "high-score-token");
        await cas.assertCookie(page);
        await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");
        assert.deepStrictEqual(verifications, ["low-score-token", "threshold-score-token", "high-score-token"]);

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
