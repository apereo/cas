const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const express = require('express');

(async () => {
    const app = express();
    await app.get('/bypass', (req, res) => res.status(400).send('Bad Request'));
    let server = app.listen(3000, async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com");
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(1000);
        await cas.log("Selecting mfa-gauth");
        await cas.assertInvisibility(page, '#mfa-gauth');
        await cas.assertInvisibility(page, '#mfa-yubikey');
        await cas.assertTicketParameter(page);
        await cas.goto(page, "https://localhost:8443/cas/login");
        await cas.assertCookie(page);
        server.close(() => {
            cas.log('Exiting server...');
            browser.close();
        });
    });

})();
