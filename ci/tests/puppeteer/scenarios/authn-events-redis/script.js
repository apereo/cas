const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const archiver = require('archiver');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    const context = browser.defaultBrowserContext();
    await context.overridePermissions("https://localhost:8443/cas/login", ['geolocation']);
    await page.setGeolocation({latitude: 90, longitude: 20});

    await cas.log("Deleting all startup events...");
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "DELETE");

    const totalAttempts = 10;
    for (let i = 1; i <= totalAttempts; i++) {
        await cas.gotoLogin(page);
        let user = (Math.random() + 1).toString(36).substring(4);
        let password = (Math.random() + 1).toString(36).substring(4);
        await cas.loginWith(page, user, password);
        await page.waitForTimeout(1000);
    }

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.log("Getting events...");

    await cas.doGet("https://localhost:8443/cas/actuator/events",
        res => {
            const count = Object.keys(res.data[1]).length;
            cas.log(`Total event records found ${count}`);
            assert(count === totalAttempts + 1);

            fs.rmSync(`${__dirname}/events.zip`, {force: true});
            const zip = fs.createWriteStream(`${__dirname}/events.zip`);
            const archive = archiver('zip', {
                zlib: { level: 9 }
            });
            archive.pipe(zip);
            res.data[1].forEach(entry => archive.append(JSON.stringify(entry), { name: `event-${entry.id}.json`}));
            archive.finalize();

        }, error => {
            throw error;
        }, {'Content-Type': "application/json"});

    await cas.log("Deleting all events...");
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "DELETE");
    await cas.log("Checking events...");
    await cas.doGet("https://localhost:8443/cas/actuator/events",
        res => assert(Object.keys(res.data[1]).length === 0), error => {
            throw error;
        }, {'Content-Type': "application/json"});

    await cas.log("Uploading events...");
    const zipFileContent = fs.readFileSync(`${__dirname}/events.zip`);
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "POST",
        {
            'Content-Length': zipFileContent.length,
            'Content-Type': 'application/octet-stream'
        }, 200,
        zipFileContent);

    fs.rmSync(`${__dirname}/events.zip`, {force: true});
    await cas.doGet("https://localhost:8443/cas/actuator/events",
        res => {
            const count = Object.keys(res.data[1]).length;
            cas.log(`Total event records found ${count}`);
            assert(count === totalAttempts + 1);
        }, error => {
            throw error;
        }, {'Content-Type': "application/json"});

    await browser.close();
})();
