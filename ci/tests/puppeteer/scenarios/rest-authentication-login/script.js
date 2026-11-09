const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");
const { parse } = require("basic-auth");

const ACCOUNT_STATES = {
    "disableduser": 403,
    "missinguser": 404,
    "expireduser": 412,
    "lockeduser": 423,
    "mustchangeuser": 428,
    "teapotuser": 418
};

async function loginAs(page, username) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, "P@ssw0rd");
    await cas.sleep(1000);
}

async function assertLoginFormError(page, username, message) {
    await loginAs(page, username);
    await cas.assertVisibility(page, "#loginErrorsPanel");
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", message);
    await cas.assertCookie(page, false);
}

(async () => {
    const received = [];
    const authenticate = (req, res, next) => {
        const credentials = parse(req.headers.authorization);
        if (credentials) {
            received.push(credentials);
        }
        if (credentials && ACCOUNT_STATES[credentials.name] !== undefined) {
            cas.log(`Answering with status ${ACCOUNT_STATES[credentials.name]} for ${credentials.name}`);
            res.status(ACCOUNT_STATES[credentials.name]).send("Account state");
        } else if (credentials && credentials.name === "warneduser" && credentials.pass === "P@ssw0rd") {
            const expiration = new Date(Date.now() + ((3 * 24) + 1) * 60 * 60 * 1000);
            res.set("X-CAS-PasswordExpirationDate", expiration.toUTCString());
            res.set("X-CAS-Warning", "rest.authn.custom.warning");
            req.principalId = "warneduser";
            next();
        } else if (!credentials || credentials.name !== "restapi" || credentials.pass !== "YdCP05HvuhOH^*Z") {
            res.set("WWW-Authenticate", "Basic realm=\"Authentication Required\"");
            res.status(401).send("Authentication Required");
        } else {
            cas.log("Authentication successful");
            req.principalId = "casuser";
            next();
        }
    };

    const app = express();
    app.post(/^(.+)$/, authenticate, (req, res) => {
        cas.log("Received request");
        const data = {
            "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
            "id": req.principalId,
            "attributes": {
                "@class": "java.util.LinkedHashMap",
                "names": [
                    "java.util.List", ["cas", "user"]
                ]
            }
        };
        res.json(data);
    });
    app.use(authenticate);

    const server = app.listen(5432, async () => {
        let failed = false;
        try {
            await cas.log("Listening...");

            const browser = await cas.newBrowser(cas.browserOptions());
            const page = await cas.newPage(browser);
            await cas.gotoLogin(page);
            await cas.loginWith(page, "restapi", "YdCP05HvuhOH^*Z");
            await cas.assertCookie(page);
            await cas.gotoLogout(page);

            await cas.log("401: FailedLoginException");
            await assertLoginFormError(page, "restapi", "Authentication attempt has failed");
            assert(received.some((c) => c.name === "restapi" && c.pass === "P@ssw0rd"));

            await cas.log("404: AccountNotFoundException");
            await assertLoginFormError(page, "missinguser", "Your account is not recognized and cannot log in at this time.");

            await cas.log("418, not mapped: FailedLoginException");
            await assertLoginFormError(page, "teapotuser", "Authentication attempt has failed");

            await cas.log("403: AccountDisabledException");
            await loginAs(page, "disableduser");
            await cas.assertInnerText(page, "#view-accountdisabled-heading", "This account has been disabled.");
            await cas.assertCookie(page, false);

            await cas.log("423: AccountLockedException");
            await loginAs(page, "lockeduser");
            await cas.assertInnerText(page, "#view-accountlocked-heading", "This account has been locked.");
            await cas.assertCookie(page, false);

            await cas.log("412: AccountExpiredException");
            await loginAs(page, "expireduser");
            await cas.assertInnerTextContains(page, "#pwdmain h2", "Your password has expired.");
            await cas.assertVisibility(page, "#pwddesc");
            await cas.assertCookie(page, false);

            await cas.log("428: AccountPasswordMustChangeException");
            await loginAs(page, "mustchangeuser");
            await cas.assertInnerTextContains(page, "#pwdmain h2", "You must change your password.");
            await cas.assertCookie(page, false);

            await cas.log("200 with X-CAS-Warning and X-CAS-PasswordExpirationDate");
            await loginAs(page, "warneduser");
            await cas.assertInnerText(page, ".loginMessageView h1", "Authentication Succeeded with Warnings");
            const warnings = await cas.innerTexts(page, ".loginMessageView .mdc-list-item__text");
            assert(warnings.some((w) => w.includes("Your password expires in 3 day(s).")), `Unexpected warnings: ${warnings}`);
            assert(warnings.some((w) => w.includes("rest.authn.custom.warning")), `Unexpected warnings: ${warnings}`);
            await cas.assertCookie(page, false);
            await cas.click(page, "#continue");
            await cas.waitForNavigation(page);
            await cas.assertCookie(page);
            await cas.assertInnerTextStartsWith(page, "#content div p", "You, warneduser, have successfully logged in");

            server.close(() => {
                cas.log("Exiting server...");
                cas.closeBrowser(browser);
            });
        } catch (e) {
            failed = true;
            throw e;
        } finally {
            if (!failed) {
                await process.exit(0);
            }
        }
    });

})();
