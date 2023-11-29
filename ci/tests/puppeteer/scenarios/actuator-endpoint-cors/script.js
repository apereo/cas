const cas = require('../../cas.js');
const express = require('express');
const path = require("path");
const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    let app = express();
    app.use(express.static(path.join(__dirname, `pages`)));
    let server = app.listen(8444, async () => {
        let failed = false;
        try {
            const browser = await puppeteer.launch(cas.browserOptions());
            const page = await cas.newPage(browser);
            await cas.goto(page, "http://localhost:8444?endpoint=info");
            await page.waitForTimeout(2500);

            let data = JSON.parse(await cas.innerText(page, "#data"));
            console.dir(data, {depth: null, colors: true});

            assert(data.cas.version !== null);
            assert(data.cas.java.vendor !== null);
            assert(data.cas.java.version !== null);

            await cas.goto(page, "http://localhost:8444?endpoint=health");
            await page.waitForTimeout(2500);
            data = JSON.parse(await cas.innerText(page, "#data"));
            console.dir(data, {depth: null, colors: true});

            assert(data.status !== null);
            assert(data.components !== null);

            server.close(() => {
                cas.log('Exiting server...');
                browser.close();
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
