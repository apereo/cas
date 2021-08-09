const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://apereo.github.io";
    const codeChallenge = "cwr1RXW4wcqyi0Eq9h1tD2tliFRYf36HMqG0lumwCtE";
    const codeVerifier = "zkuyfY0CcG1yuVojREYwtbnpjOsOleD.OWkBpNVTHKyABMJ0ly_ZKTeOi."
        + "STPvshXsHyShcyAzm6z4ThKr2Y91RKFLvmOkJEiBhaSzIp~YHH3wkrzlB6m~y8h~td_pPg";

    let url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&code_challenge=${codeChallenge}&code_challenge_method=S256&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;

    console.log(`Navigating to ${url}`);
    await page.goto(url);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.click(page, "#allow");
    await page.waitForNavigation();

    let code = await cas.assertParameter(page, "code");
    console.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += `code_verifier=${codeVerifier}&`;
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
        assert(decoded.sub !== null)
        assert(decoded.aud !== null)
        assert(decoded.state !== null)
        assert(decoded.nonce !== null)
        assert(decoded.client_id !== null)
        assert(decoded["preferred_username"] == null)
    }, function (error) {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken != null, "Access Token cannot be null")
    await browser.close();
})();
