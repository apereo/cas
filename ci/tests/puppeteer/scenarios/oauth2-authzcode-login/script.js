const puppeteer = require('puppeteer');
const https = require('https');
const assert = require('assert');
const axios = require('axios');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "http://localhost:8080";
    await page.goto(url);

    await page.waitForTimeout(1000)
    await cas.type(page,'#authorization-endpoint-input', "https://localhost:8443/cas/oauth2.0/authorize");
    await cas.type(page,'#client-id-input', "f28ace49");
    await cas.type(page,'#scope-input', "profile");
    await cas.clickLast(page, "form button")
    await page.waitForTimeout(3000)

    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(3000)
    await cas.type(page,'#token-endpoint-input', "https://localhost:8443/cas/oauth2.0/token");
    await cas.type(page,'#client-secret-input', "8f278d1b975f");
    await cas.clickLast(page, "form button")
    await page.waitForTimeout(3000)

    const token = await cas.inputValue(page, "#access-token-input");
    assert(token != null)

    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    const params = new URLSearchParams()
    params.append('access_token', token)

    instance
        .post('https://localhost:8443/cas/oauth2.0/profile', params)
        .then(res => {
            console.log(res.data);
            let result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "f28ace49");
            assert(result.service === "http://localhost:8080");
        })
        .catch(error => {
            throw error;
        })
    
    await browser.close();
})();
