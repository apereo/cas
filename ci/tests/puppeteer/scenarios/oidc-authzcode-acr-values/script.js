const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const https = require('https');
const assert = require('assert');
const axios = require('axios');
const jwt = require('jsonwebtoken');

const redirectUrl = "https://apereo.github.io";

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

async function fetchScratch(page) {
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
    return JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);
}

async function fetchCode(page, acr) {
    let url = "https://localhost:8443/cas/oidc/authorize?"
        + "response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&"
        + "prompt=login&redirect_uri=" + redirectUrl
        + "&nonce=3d3a7457f9ad3&state=1735fd6c43c14&acr_values=" + acr;

    console.log("Navigating to " + url);
    await page.goto(url);
    await cas.loginWith(page, "casuser", "Mellon");

    let scratch = await fetchScratch(page);
    console.log("Using scratch code " + scratch + " to login...");
    await cas.type(page,'#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    console.log("Page url " + page.url())
    let result = new URL(page.url());
    assert(result.searchParams.has("code"));
    let code = result.searchParams.get("code");
    console.log("OAuth code " + code);
    return code;
}

async function exchangeCode(page, code, successHandler) {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += "redirect_uri=" + redirectUrl;

    let accessTokenUrl = 'https://localhost:8443/cas/oidc/token?' + accessTokenParams + "&code=" + code;
    console.log("Calling " + accessTokenUrl);

    let accessToken = null;
    await instance
        .post(accessTokenUrl, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.data.access_token !== null);

            accessToken = res.data.access_token;
            console.log("Received access token " + accessToken);

            console.log("Decoding ID token...");
            let decoded = jwt.decode(res.data.id_token);
            console.log(decoded);

            successHandler(decoded);
        })
        .catch(error => {
            throw 'Operation failed to obtain access token: ' + error;
        })
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    // let code = await fetchCode(page, "1%202");

    console.log("Fetching code for MFA based on ACR mfa-gauth")
    let code = await fetchCode(page, "mfa-gauth");
    await exchangeCode(page, code, function (idToken) {
        assert(idToken.sub !== null)
        assert(idToken.acr === "mfa-gauth")
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    })
    
    console.log("Fetching code for MFA based on ACR 1 mapped in configuration to mfa-gauth")
    code = await fetchCode(page, "1%202");
    await exchangeCode(page, code, function (idToken) {
        assert(idToken.sub !== null)
        assert(idToken.acr === "1")
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    })
    await browser.close();
})();
