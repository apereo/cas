
const cas = require("../../cas.js");
const express = require("express");

(async () => {
    const app = express();
    app["get"]("/ip", (req, res) => res.status(502).send("1"));
    const server = app.listen(5423, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);

        await cas.gotoLogin(page);
        await cas.sleep(2000);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");
        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });

})();
