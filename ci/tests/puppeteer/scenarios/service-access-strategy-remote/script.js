
const cas = require("../../cas.js");
const express = require("express");
const assert = require("assert");

(async () => {
    const app = express();
    let slowRequests = 0;
    app.get("/gateway", (req, res) => {
        const username = req.query.username;

        if (username === "casslow") {
            slowRequests++;
            setTimeout(() => res.status(200).send("Accepted too late"), 8000);
        } else if (username === "casblock") {
            res.status(403).send("Denied");
        } else {
            res.status(200).send("Accepted");
        }
    });

    const server = app.listen(5566, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, "https://localhost:9859/anything/deny");
        const response = await cas.loginWith(page, "casblock");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
        assert(response.status() === 401);

        await cas.log("Endpoint answering after the fixed timeout denies access, without retries");
        await cas.gotoLogin(page, "https://localhost:9859/anything/slow");
        const slowResponse = await cas.loginWith(page, "casslow");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
        assert(slowResponse.status() === 401);
        assert(slowRequests === 1);

        await cas.gotoLogin(page, "https://localhost:9859/anything/OK");
        await cas.loginWith(page, "casuser");
        await cas.sleep(1000);
        await cas.assertTicketParameter(page);
        
        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
