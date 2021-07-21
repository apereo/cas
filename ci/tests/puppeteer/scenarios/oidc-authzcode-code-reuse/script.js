const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const https = require('https');
const assert = require('assert');
const axios = require('axios');
const jwt = require('jsonwebtoken');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://github.com/apereo/cas";

    let url = "https://localhost:8443/cas/oidc/authorize?"
        + "response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&"
        + "redirect_uri=" + redirectUrl
        + "&nonce=3d3a7457f9ad3&state=1735fd6c43c14";

    console.log("Navigating to " + url);
    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000)
    await cas.click(page, "#allow");
    await page.waitForNavigation();

    console.log("Page url " + page.url())
    let result = new URL(page.url());
    assert(result.searchParams.has("code"));
    let code = result.searchParams.get("code");
    console.log("OAuth code " + code);

    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=authorization_code&";
    params += "redirect_uri=" + redirectUrl;
    params += "&code=" + code;

    url = 'https://localhost:8443/cas/oidc/token?' + params;
    console.log("Calling " + url);

    let accessToken = null;

    await instance
        .post(url, new URLSearchParams(), {
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

            assert(decoded.sub !== null)
            assert(decoded["preferred_username"] == null)
        })
        .catch(error => {
            throw 'Operation failed to obtain access token: ' + error;
        })

    assert(accessToken != null, "Access Token cannot be null")

    params = "access_token=" + accessToken;
    url = 'https://localhost:8443/cas/oidc/profile?' + params;
    console.log("Calling profile " + url);

    instance
        .post(url, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            console.log(res.data);
            assert(res.data.email != null)
            assert(res.data.gender != null)
            assert(res.data.name != null)
            assert(res.data["preferred_username"] != null)
        })
        .catch(error => {
            throw 'Operation failed: ' + error;
        })

    await browser.close();
})();
