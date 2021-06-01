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
    const response = await httpGet(options1);
    var scratch=JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);

    const page = await browser.newPage();
    const service="https://example.com";
    await page.goto("https://localhost:8443/cas/login?service="+service);
    await page.bringToFront();
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(500);
    
    console.log("Select mfa-gauth");
    await cas.assertVisibility(page, '#mfa-gauth');
    
    await page.$eval('#mfa-gauth > form[name=fm1]', form => form.submit());
    await page.waitForTimeout(1000);
    
    console.log("Using scratch code " + scratch +" to login...");
    await page.type('#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000);

    console.log("Catching generated ticket");
    let result = new URL(page.url());
    let ticket = result.searchParams.get("ticket");
    assert(ticket != null);
    
    console.log("Validating ticket " + ticket + " with service " + service);
    let options2 = {
        protocol: "https:",
        hostname: "localhost",
        port: 8443,
        path: "/cas/p3/serviceValidate?service=" + service + "&ticket=" + ticket,
        method: "GET",
        rejectUnauthorized: false,
    };
    const body = await httpGet(options2);

    console.log(body);
    assert(body.includes("<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"))
    assert(body.includes("<cas:authenticationSuccess>"))
    assert(body.includes("<cas:user>casuser</cas:user>"))
    assert(body.includes("<cas:isFromNewLogin>true</cas:isFromNewLogin>"))

    assert(body.includes("<cas:credentialType>GoogleAuthenticatorTokenCredential</cas:credentialType>"))
    assert(body.includes("<cas:authenticationMethod>GoogleAuthenticatorAuthenticationHandler</cas:authenticationMethod>"))
    assert(body.includes("<cas:successfulAuthenticationHandlers>GoogleAuthenticatorAuthenticationHandler</cas:successfulAuthenticationHandlers>"))

    assert(body.includes("<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>"))

    await browser.close();
})();
