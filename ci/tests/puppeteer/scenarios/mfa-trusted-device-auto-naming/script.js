const puppeteer = require("puppeteer");
const assert = require("assert");
const url = require("url");
const cas = require("../../cas.js");
const https = require("https");

const httpGet = (options) => {
    return new Promise((resolve, reject) => {
        https.get(options, res => {
            res.setEncoding("utf8");
            const body = [];
            res.on("data", chunk => body.push(chunk));
            res.on("end", () => resolve(body.join("")));
        }).on("error", reject);
    });
};


(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    
    console.log("Fetching Scratch codes from /cas/actuator...");

    let options1 = {
        protocol: "https:",
        hostname: "localhost",
        port: 8443,
        path: "/cas/actuator/gauthCredentialRepository/casuser",
        method: "GET",
        rejectUnauthorized: false,
    };
    let response = await httpGet(options1);
    let scratch = JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);

    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    
    console.log("Using scratch code " + scratch + " to login...");
    await cas.type(page,'#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    let element = await cas.innerText(page, '#content div h2');
    assert(element === "Log In Successful")

    await cas.assertTicketGrantingCookie(page);

    options1 = {
        protocol: "https:",
        hostname: "localhost",
        port: 8443,
        path: "/cas/actuator/multifactorTrustedDevices",
        method: "GET",
        rejectUnauthorized: false,
    };
    response = await httpGet(options1);
    let record = JSON.parse(response)[0];
    assert(record.id !== null);
    assert(record.name !== null);
    await browser.close();
})();
