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

    let accessTokenParms = "client_id=client&";
    accessTokenParms += "client_secret=secret&";
    accessTokenParms += "grant_type=authorization_code&";
    accessTokenParms += "redirect_uri=" + redirectUrl;

    let accessTokenUrl = 'https://localhost:8443/cas/oidc/token?' + accessTokenParms + "&code=" + code;
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

            assert(decoded.sub !== null)
            assert(decoded["preferred_username"] == null)
        })
        .catch(error => {
            throw 'Operation failed to obtain access token: ' + error;
        })

    assert(accessToken != null, "Access Token cannot be null")

    let profileUrl = "https://localhost:8443/cas/oidc/profile?access_token=" + accessToken;
    console.log("Calling user profile " + profileUrl);
    instance
        .post(profileUrl, new URLSearchParams(), {
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

    console.log("Trying to re-use OAuth code " + accessTokenUrl);
    await instance
        .post(accessTokenUrl, new URLSearchParams(), {
            headers: {
                'Content-Type': "application/json"
            }
        })
        .then(res => {
            throw 'OAuth code ' + code + ' cannot be used again';
        })
        .catch(error => {
            console.log(error.response.data)
            assert(error.response.data.error === 'invalid_grant')
        })

    await browser.close();
})();
