const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

const redirectUrl = "https://apereo.github.io";

async function fetchCode(page, acr, params) {
    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&nonce=3d3a7457f9ad3&state=1735fd6c43c14&acr_values=${acr}`;
    if (params !== undefined) {
        url += `&${params}`;
    }

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await page.waitForTimeout(3000)
    }

    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.screenshot(page);
    await page.waitForTimeout(2000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForTimeout(3000)
    }
    await cas.screenshot(page);
    await cas.type(page, '#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
        await page.waitForTimeout(2000)
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);
    return code;
}

async function exchangeCode(page, code, successHandler) {
    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUrl}`;

    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, async res => {
        await cas.log(res.data);
        assert(res.data.access_token !== null);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        successHandler(decoded);
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/logout");

    await cas.log("===================================================================");
    await cas.logg("Fetching code for MFA based on ACR mfa-gauth");
    let code = await fetchCode(page, "mfa-gauth", "login=prompt");
    await exchangeCode(page, code, idToken => {
        assert(idToken.sub !== null);
        assert(idToken.acr === "https://refeds.org/profile/mfa");
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    });
    await cas.goto(page, "https://localhost:8443/cas/logout");

    await cas.log("===================================================================");
    await cas.logg("Fetching code for MFA based on ACR 1 mapped in configuration to mfa-gauth");
    code = await fetchCode(page, "https://refeds.org/profile/mfa", "login=prompt");
    await exchangeCode(page, code, idToken => {
        assert(idToken.sub !== null);
        assert(idToken.acr === "https://refeds.org/profile/mfa");
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    });
    await cas.goto(page, "https://localhost:8443/cas/logout");

    await cas.log("===================================================================");
    await cas.logg("Fetching code for MFA based on ACR mfa-gauth for existing SSO");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    code = await fetchCode(page, "mfa-gauth");
    await exchangeCode(page, code, idToken => {
        assert(idToken.sub !== null);
        assert(idToken.acr === "https://refeds.org/profile/mfa");
        assert(idToken.amr.includes("GoogleAuthenticatorAuthenticationHandler"))
    });
    await browser.close();
})();
