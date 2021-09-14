const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

const redirectUrl = "https://apereo.github.io";

async function fetchCode(page, acr, params) {
    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14&acr_values=${acr}`;
    if (params !== undefined) {
        url += `&${params}`;
    }

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page, "casuser", "Mellon");
    }

    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    console.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page, '#token', scratch);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    console.log(`OAuth code ${code}`);
    return code;
}

async function exchangeCode(page, code, successHandler) {
    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    console.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, async function (res) {
        console.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
        console.log(`Received access token ${accessToken}`);

        console.log("Decoding ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        successHandler(decoded);
    }, function (error) {
        throw `Operation failed to obtain access token: ${error}`;
    });
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Fetching code for MFA based on ACR mfa-gauth")
    let code = await fetchCode(page, "mfa-gauth", "login=prompt");
    await exchangeCode(page, code, function (idToken) {
        assert(idToken.sub !== null)
        assert(idToken.acr === "https://refeds.org/profile/mfa")
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    })

    await page.goto("https://localhost:8443/cas/logout");

    console.log("Fetching code for MFA based on ACR 1 mapped in configuration to mfa-gauth")
    code = await fetchCode(page, "https://refeds.org/profile/mfa%20something-else", "login=prompt");
    await exchangeCode(page, code, function (idToken) {
        assert(idToken.sub !== null)
        assert(idToken.acr === "https://refeds.org/profile/mfa")
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    })
    await page.goto("https://localhost:8443/cas/logout");

    console.log("Fetching code for MFA based on ACR mfa-gauth for existing SSO")
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    code = await fetchCode(page, "mfa-gauth");
    await exchangeCode(page, code, function (idToken) {
        assert(idToken.sub !== null)
        assert(idToken.acr === "https://refeds.org/profile/mfa")
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    })
    await browser.close();
})();
