
const cas = require("../../cas.js");
const express = require("express");
const assert = require("assert");

(async () => {
    const app = express();
    app.get("/gateway", (req, res) => {
        const username = req.query.username;

        if (username === "casblock") {
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

        await cas.gotoLogin(page, "https://localhost:9859/anything/OK");
        await cas.loginWith(page, "casuser");
        await cas.sleep(1000);
        await cas.assertTicketParameter(page);
        
        server.close(() => {
            cas.log("Exiting server...");
            browser.close();
        });
    });
})();
