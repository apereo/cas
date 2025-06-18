
const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

(async () => {
    let aupAccepted = false;

    const app = express();
    app.post("/aup", (req, res) => {
        cas.log("Accepting AUP...");
        aupAccepted = true;
        res.status(200).send("Accepted");
    });
    app.get("/aup/status", (req, res) => {
        cas.log(`AUP status: ${aupAccepted}`);
        if (aupAccepted) {
            res.status(202).send("Accepted");
        } else {
            res.status(403).send("Denied");
        }
    });
    app.get("/aup/policy", (req, res) => {
        cas.log("Received AUP policy terms request");
        const data = {
            "@class": "org.apereo.cas.aup.AcceptableUsagePolicyTerms",
            "code": "screen.aup.policyterms.some.key",
            "defaultText": "Default policy text"
        };
        res.json(data);
    });
    
    const server = app.listen(5544, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        const service = "http://localhost:9889/anything/app1";
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy");
        await cas.assertVisibility(page, "button[name=submit]");
        await cas.assertVisibility(page, "button[name=cancel]");
        await cas.sleep(1000);
        await cas.click(page, "#aupSubmit");
        await cas.waitForNavigation(page);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "casuser");

        await cas.log("Logging in again, now with SSO");
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        await cas.assertTicketParameter(page);
        await cas.gotoLogout(page);

        await cas.log("Logging in again, now without SSO");
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.assertTicketParameter(page);

        server.close(() => {
            cas.log("Exiting server...");
            browser.close();
        });
    });
})();
