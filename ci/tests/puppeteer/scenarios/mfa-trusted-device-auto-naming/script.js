
const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const os = require("os");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await cas.log("Fetching Scratch codes from /cas/actuator...");
    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();

    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,"#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    
    await cas.assertCookie(page);

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    let response = await cas.doRequest(baseUrl);
    let record = JSON.parse(response)[0];
    assert(record.id !== undefined);
    assert(record.name !== undefined);

    response = await cas.doRequest(`${baseUrl}/${record.principal}`);
    record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id !== undefined);
    assert(record.name !== undefined);

    await cas.doGet(`${baseUrl}/export`,
        (res) => {
            const tempDir = os.tmpdir();
            const exported = path.join(tempDir, "trusteddevices.zip");
            res.data.pipe(fs.createWriteStream(exported));
            cas.log(`Exported records are at ${exported}`);
        },
        (error) => {
            throw error;
        }, {}, "stream");

    const template = path.join(__dirname, "device-record.json");
    const body = fs.readFileSync(template, "utf8");
    await cas.log(`Import device record:\n${body}`);
    await cas.doRequest(`${baseUrl}/import`, "POST", {
        "Accept": "application/json",
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 201, body);

    await cas.closeBrowser(browser);
})();
