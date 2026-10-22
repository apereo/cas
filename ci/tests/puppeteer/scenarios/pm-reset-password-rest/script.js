const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

const USERNAME = "casuser";
const EMAIL = "casuser@example.org";

(async () => {
    const lookups = [];
    const changes = [];
    const app = express();
    app.use(express.json());
    app.get("/pm/user", (req, res) => {
        lookups.push(`user:${req.query.email}`);
        if (req.query.email === EMAIL) {
            res.type("text/plain").send(USERNAME);
        } else {
            res.sendStatus(404);
        }
    });
    app.get("/pm/email", (req, res) => {
        lookups.push(`email:${req.query.username}`);
        if (req.query.username === USERNAME) {
            res.type("text/plain").send(EMAIL);
        } else {
            res.sendStatus(404);
        }
    });
    app.get("/pm/questions", (req, res) => {
        lookups.push(`questions:${req.query.username}`);
        res.json({"What is your favorite city?": "Rome"});
    });
    app.post("/pm/change", (req, res) => {
        cas.log(`Changing password for ${req.body.username}`);
        changes.push(req.body);
        res.json(true);
    });
    const server = await new Promise((resolve) => {
        const started = app.listen(5615, () => resolve(started));
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("The username is looked up by email through the REST backend");
        await cas.gotoLogin(page);
        await cas.click(page, "#forgotUsernameLink");
        await cas.sleep(1000);
        await cas.type(page, "#email", EMAIL);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.assertInnerTextStartsWith(page, "#content h2", "Instructions Sent Successfully.");
        assert(lookups.includes(`user:${EMAIL}`), "The email address must be sent to look up the username");
        const usernameEmail = await cas.extractFromEmail(browser);
        assert(usernameEmail.includes(`Username:${USERNAME}`), `Unexpected email ${usernameEmail}`);

        await cas.logg("The password is reset through the REST backend");
        await cas.gotoLogin(page);
        await cas.click(page, "#forgotPasswordLink");
        await cas.sleep(1000);
        await cas.type(page, "#username", USERNAME);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
        assert(lookups.includes(`email:${USERNAME}`));

        const link = await cas.extractFromEmail(browser);
        await cas.goto(page, link);
        await cas.sleep(2000);
        await cas.assertInnerText(page, "#content h2", "Answer Security Questions");
        assert(lookups.includes(`questions:${USERNAME}`));
        await cas.type(page, "#q0", "Rome", true);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(2000);
        await cas.assertInnerText(page, "#pwdmain h2", `Hello, ${USERNAME}. You must change your password.`);

        const newPassword = await cas.randomWord();
        await cas.type(page, "#password", newPassword, true);
        await cas.type(page, "#confirmedPassword", newPassword, true);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(2000);
        await cas.assertInnerText(page, "#content h2", "Password Change Successful");
        assert(changes.length === 1);
        assert(changes[0].username === USERNAME);
        assert(changes[0].password === newPassword);
    } finally {
        await cas.closeBrowser(browser);
        server.close();
    }
})();
