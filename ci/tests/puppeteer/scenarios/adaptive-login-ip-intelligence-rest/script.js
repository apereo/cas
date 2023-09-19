const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const express = require('express');

(async () => {
    let app = express();
    app["get"]("/ip", (req, res) => res.status(502).send("1"));
    let server = app.listen(5423, async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);

        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(2000);
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(2000);
        await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");
        server.close(() => {
            cas.log('Exiting server...');
            browser.close();
        });
    });

})();
