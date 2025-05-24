
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const codeChallenge = "cwr1RXW4wcqyi0Eq9h1tD2tliFRYf36HMqG0lumwCtE";
    const codeVerifier = "zkuyfY0CcG1yuVojREYwtbnpjOsOleD.OWkBpNVTHKyABMJ0ly_ZKTeOi."
        + "STPvshXsHyShcyAzm6z4ThKr2Y91RKFLvmOkJEiBhaSzIp~YHH3wkrzlB6m~y8h~td_pPg";
    const clientId = "client";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=`
        + `openid%20email%20profile%20address%20phone&redirect_uri=${redirectUrl}&code_challenge=${codeChallenge}`
        + "&code_challenge_method=S256&nonce=3d3a7457f9ad3&state=1735fd6c43c14";

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.loginWith(page);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = `client_id=client&grant_type=authorization_code&redirect_uri=${redirectUrl}`;
    let accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl} without a code verifier parameter`);
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        throw `Operation should fail to obtain access token: ${res}`;
    }, (error) => {
        cas.log(`Expected status code: ${error.response.status}`);
        assert(error.response.status === 401);
    });

    accessTokenParams = `client_id=client&code_verifier=${codeVerifier}&grant_type=authorization_code&redirect_uri=${redirectUrl}`;
    accessTokenUrl = `https://localhost:8443/cas/oidc/token?${accessTokenParams}&code=${code}`;
    await cas.log(`Calling ${accessTokenUrl}`);

    let accessToken = null;
    await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        accessToken = res.data.access_token;
        await cas.log(`Received access token ${accessToken}`);

        await cas.log("Decoding ID token...");
        const decoded = await cas.decodeJwt(res.data.id_token);
        assert(decoded.sub !== undefined);
        assert(decoded.aud !== undefined);
        assert(decoded.state !== undefined);
        assert(decoded.nonce !== undefined);
        assert(decoded.client_id !== undefined);
        assert(decoded["preferred_username"] === undefined);
    }, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });

    assert(accessToken !== undefined, "Access Token cannot be null");

    const value = `${clientId}:`;
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    
    await cas.log(`Introspecting token ${accessToken}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${accessToken}`,
        (res) => assert(res.data.active === true), (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
    
    await browser.close();
})();
