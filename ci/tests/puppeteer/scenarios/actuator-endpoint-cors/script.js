const cas = require("../../cas.js");
const express = require("express");
const path = require("path");

const assert = require("assert");

(async () => {
    const app = express();
    app.use(express.static(path.join(__dirname, "pages")));
    const server = app.listen(8444, async () => {
        let failed = false;
        try {
            const browser = await cas.newBrowser(cas.browserOptions());
            const page = await cas.newPage(browser);
            await cas.goto(page, "http://localhost:8444?endpoint=info");
            await cas.sleep(2500);

            let data = JSON.parse(await cas.innerText(page, "#data"));
            console.dir(data, {depth: null, colors: true});

            assert(data.cas.version !== undefined);
            assert(data.cas.java.vendor !== undefined);
            assert(data.cas.java.version !== undefined);

            await cas.goto(page, "http://localhost:8444?endpoint=health");
            await cas.sleep(2500);
            data = JSON.parse(await cas.innerText(page, "#data"));
            console.dir(data, {depth: null, colors: true});

            assert(data.status !== undefined);
            assert(data.components !== undefined);

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
