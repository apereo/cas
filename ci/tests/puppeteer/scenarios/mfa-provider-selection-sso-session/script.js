const puppeteer = require("puppeteer");
const assert = require("assert");
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

    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);

    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);

    await page.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page.waitForTimeout(500);
    await cas.assertInvisibility(page, "#username")
    
    console.log("Selecting mfa-gauth");
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');

    await cas.submitForm(page, "#mfa-gauth > form[name=fm1]")
    await page.waitForTimeout(500);

    console.log("Fetching Scratch codes from /cas/actuator...");
    let options1 = {
        protocol: "https:",
        hostname: "localhost",
        port: 8443,
        path: "/cas/actuator/gauthCredentialRepository/casuser",
        method: "GET",
        rejectUnauthorized: false,
    };
    const response = await httpGet(options1);
    let scratch = JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);

    console.log("Using scratch code " + scratch + " to login...");
    await cas.type(page,'#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    const url = await page.url()
    console.log(`Page url: ${url}`)
    await cas.assertTicketParameter(page);

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);

    let context = await cas.innerText(page, "#authnContextClass td.attribute-value")
    console.log(`Authentication context class ${context}`);
    assert(context === "[mfa-gauth]")

    await page.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page.waitForTimeout(500);
    await page.waitForNavigation();

    const url2 = await page.url()
    console.log(`Page url: ${url2}`)
    await cas.assertTicketParameter(page);

    await browser.close();
})();
