const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const express = require("express");

(async () => {
    const app = express();
    app.get("/gateway", (req, res) => {
        console.log(req.query);
        const username = req.query.username;

        if (username === "casblock") {
            res.status(403).send("Denied");
        } else {
            res.status(200).send("Accepted");
        }
    });

    const server = app.listen(5566, async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, "https://localhost:9859/anything/deny");
        await cas.loginWith(page, "casblock");
        await cas.waitForTimeout(page, 1000);
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");

        await cas.gotoLogin(page, "https://localhost:9859/anything/OK");
        await cas.loginWith(page, "casuser");
        await cas.waitForTimeout(page, 1000);
        await cas.assertTicketParameter(page);
        
        server.close(() => {
            cas.log("Exiting server...");
            browser.close();
        });
    });
})();
