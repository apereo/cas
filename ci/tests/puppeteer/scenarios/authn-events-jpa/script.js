const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.log("Deleting all startup events...");
    await cas.doDelete("https://localhost:8443/cas/actuator/events");
    fs.rmSync(`${__dirname}/events.zip`, {force: true});
    
    const totalAttempts = 2;
    for (let i = 1; i <= totalAttempts; i++) {
        await page.setExtraHTTPHeaders({
            "X-Forwarded-For": "192.206.151.131"
        });
        await cas.gotoLogin(page);
        const user = (Math.random() + 1).toString(36).substring(4);
        const password = (Math.random() + 1).toString(36).substring(4);
        await cas.loginWith(page, user, password);
        await cas.sleep(500);
    }
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.log("Getting events...");

    await cas.doGet("https://localhost:8443/cas/actuator/events",
        async (res) => {
            const count = res.data.length;
            await cas.log(`Total event records found ${count}`);
            assert(count > totalAttempts + 1);
            fs.rmSync(`${__dirname}/events.zip`, {force: true});

            await cas.createZipFile(`${__dirname}/events.zip`, (archive) => res.data.forEach((entry) => archive.append(JSON.stringify(entry), {name: `event-${entry.id}.json`})));
        }, async (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.log("Deleting all events...");
    await cas.doDelete("https://localhost:8443/cas/actuator/events");
    await cas.log("Checking events...");
    await cas.doGet("https://localhost:8443/cas/actuator/events",
        async (res) => assert(res.data.length === 0), async (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.log("Uploading events...");
    const zipFileContent = fs.readFileSync(`${__dirname}/events.zip`);
    await cas.doRequest("https://localhost:8443/cas/actuator/events", "POST",
        {
            "Content-Length": zipFileContent.length,
            "Content-Type": "application/octet-stream"
        }, 200,
        zipFileContent);

    fs.rmSync(`${__dirname}/events.zip`, {force: true});
    await cas.doGet("https://localhost:8443/cas/actuator/events",
        async (res) => {
            const count = res.data.length;
            await cas.log(`Total event records found ${count}`);
            assert(count > totalAttempts + 1);
        }, async (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.closeBrowser(browser);
})();
