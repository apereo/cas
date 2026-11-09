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
    }, "cf-turnstile-response", value);
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
        cas.log(`Received Turnstile verification request for token ${token}`);
        res.json({success: token === "valid-turnstile-token"});
    });

    const server = app.listen(5611, async () => {
        const service = "https://localhost:9859/anything/cas";

        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.sleep(2000);
        await cas.assertInvisibility(page, "#turnstileSection");

        await cas.gotoLogin(page, service);
        await cas.sleep(4000);
        await cas.assertVisibility(page, "#turnstileSection");

        await loginWithCaptcha(page, "invalid-turnstile-token");
        await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "reCAPTCHA’s validation failed.");
        await cas.assertCookie(page, false);

        await loginWithCaptcha(page, "valid-turnstile-token");
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        assert.equal(json.serviceResponse.authenticationSuccess.user, "casuser");
        assert.deepStrictEqual(verifications, ["invalid-turnstile-token", "valid-turnstile-token"]);

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
