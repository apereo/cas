const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const express = require('express');

(async () => {
    const app = express();
    await app.get('/bypass', (req, res) => res.status(400).send('Bad Request'));
    let server = app.listen(3000, async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, "https://google.com");
        await cas.loginWith(page);
        await page.waitForTimeout(1000);
        await cas.log("Selecting mfa-gauth");
        await cas.assertInvisibility(page, '#mfa-gauth');
        await cas.assertInvisibility(page, '#mfa-yubikey');
        await cas.assertTicketParameter(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page);
        server.close(() => {
            cas.log('Exiting server...');
            browser.close();
        });
    });

})();
