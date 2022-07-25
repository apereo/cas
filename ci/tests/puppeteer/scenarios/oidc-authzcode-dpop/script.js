const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');
const jose = require('jose');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code"
        + "&client_id=client&scope=openid%20profile&"
        + "redirect_uri=https://apereo.github.io";

    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let dt = new Date();
    dt.setSeconds(dt.getSeconds() - 5);
    const payload = {
        "htm": "POST",
        "htu": "https://localhost:8443/cas/oidc/token",
        "iat": dt.getTime() / 1000,
        "jti": "vqv2EAaJECl67LmE"
    };
    console.log(`DPoP proof payload is`);
    console.log(payload);

    const {publicKey, privateKey} = await jose.generateKeyPair('ES256');
    const publicJwk = await jose.exportJWK(publicKey);
    console.log(`DPoP public key is`);
    console.log(publicJwk);

    const dpopProof = await cas.createJwt(payload, privateKey, "ES256",
        {
            header: {
                jwk: publicJwk,
                typ: "dpop+jwt"
            }
        });

    console.log(`DPoP JWT is ${dpopProof}`);
    let code = await cas.assertParameter(page, "code");
    console.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token`;
    const params = `grant_type=authorization_code&client_id=client&redirect_uri=https://apereo.github.io&code=${code}`;

    let accessToken = null;
    await cas.doPost(accessTokenUrl, params, {
        "DPoP": dpopProof
    }, res => {
        accessToken = res.data.access_token;
        
        assert(accessToken !== null);
        assert(res.data.token_type === "DPoP");

        cas.decodeJwt(accessToken, true)
            .then(decoded => {
                assert(decoded !== null);
                console.log(decoded);
                assert(decoded.payload["DPoP"] !== undefined);
                assert(decoded.payload["DPoPConfirmation"] !== undefined);
                assert(decoded.payload["cnf"]["jkt"] !== undefined)
            });
    }, error => {
        throw `Operation failed: ${error}`;
    });


    let sha256 = await cas.sha256(accessToken);
    let base64Token = await cas.base64Url(sha256);
    const profilePayload = {
        "htm": "POST",
        "htu": "https://localhost:8443/cas/oidc/profile",
        "iat": dt.getTime() / 1000,
        "jti": "vqv2EAaJECl67LmE",
        "ath": base64Token
    };

    console.log(`DPoP proof profile payload is`);
    console.log(profilePayload);

    const dpopProofProfile = await cas.createJwt(profilePayload, privateKey, "ES256",
        {
            header: {
                jwk: publicJwk,
                typ: "dpop+jwt"
            }
        });
    console.log(`DPoP JWT is ${dpopProofProfile}`);
    
    let profileUrl = `https://localhost:8443/cas/oidc/profile?token=${accessToken}`;
    console.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json",
        "DPoP": dpopProofProfile
    }, res => {
        console.log(res.data);
        assert(res.data.sub != null)
    }, error => {
        throw `Operation failed: ${error}`;
    });


    await browser.close();
})();
