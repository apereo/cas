const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const archiver = require('archiver');
const fs = require('fs');

async function getAllEvents() {
    return JSON.parse(await cas.doRequest("https://localhost:8443/cas/actuator/events", "GET", {'Content-Type': "application/json"}, 200));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    const context = browser.defaultBrowserContext();
    await context.overridePermissions("https://localhost:8443/cas/login", ['geolocation']);
    await page.setGeolocation({latitude: 90, longitude: 20});

    await cas.log("Deleting all startup events...");
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "DELETE");

    const totalAttempts = 2;
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
    let body = await getAllEvents();

    let count = Object.keys(body[1]).length;
    await cas.log(`Total event records found ${count}`);
    assert(count === totalAttempts + 1);

    fs.rmSync(`${__dirname}/events.zip`, {force: true});
    const zip = fs.createWriteStream(`${__dirname}/events.zip`);
    const archive = archiver('zip', {
        zlib: { level: 9 }
    });
    archive.pipe(zip);
    body[1].forEach(entry => archive.append(JSON.stringify(entry), { name: `event-${entry.id}.json`}));
    await archive.finalize();
    
    await cas.log("Deleting all events...");
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "DELETE");
    await cas.log("Checking events...");
    body = await getAllEvents();
    assert(Object.keys(body[1]).length === 0);

    await cas.log("Uploading events...");
    const zipFileContent = fs.readFileSync(`${__dirname}/events.zip`);
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "POST",
        {
            'Content-Length': zipFileContent.length,
            'Content-Type': 'application/octet-stream'
        }, 200,
        zipFileContent);

    fs.rmSync(`${__dirname}/events.zip`, {force: true});
    body = await getAllEvents();
    count = Object.keys(body[1]).length;
    await cas.log(`Total event records found ${count}`);
    assert(count === totalAttempts + 1);

    await browser.close();
})();
