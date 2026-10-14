
const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const scopes = `${encodeURIComponent("openid profile")}`;
    const {publicKey, privateKey} = await jose.generateKeyPair("ES256", { extractable: true });
    const publicJwk = await jose.exportJWK(publicKey);
    const thumbprint = await jose.calculateJwkThumbprint(publicJwk, "sha256");
    await cas.log(`Binding the authorization code to DPoP key ${thumbprint}`);
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${scopes}&redirect_uri=${redirectUrl}&dpop_jkt=${thumbprint}`;

    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const dt = new Date();
    dt.setSeconds(dt.getSeconds() - 5);
    const payload = {
        "htm": "POST",
        "htu": "https://localhost:8443/cas/oidc/token",
        "iat": dt.getTime() / 1000,
        "jti": await cas.randomWord(16, false)
    };
    await cas.log("DPoP proof payload is");
    await cas.log(payload);

    await cas.log("DPoP public key is");
    await cas.log(publicJwk);

    const privateKeyPem = await jose.exportPKCS8(privateKey);
    await cas.log("DPoP private key is");
    
    const dpopProof = await cas.createJwt(payload, privateKeyPem, "ES256",
        {
            header: {
                jwk: publicJwk,
                typ: "dpop+jwt"
            }
        });

    await cas.log(`DPoP JWT is ${dpopProof}`);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token";
    const params = `grant_type=authorization_code&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;

    await cas.log("A token request without a DPoP proof cannot redeem the bound code");
    await cas.doPost(accessTokenUrl, params, {}, () => {
        throw "Token request without a DPoP proof must fail";
    }, (error) => {
        assert.equal(error.response.status, 400);
        assert(error.response.data.error === "invalid_dpop_proof");
    });

    await cas.log("A DPoP header that is not a JWT is an invalid proof, not a malformed request");
    await cas.doPost(accessTokenUrl, params, {
        "DPoP": "not-a-jwt"
    }, () => {
        throw "Token request with a malformed DPoP proof must fail";
    }, (error) => {
        assert.equal(error.response.status, 400);
        assert(error.response.data.error === "invalid_dpop_proof");
    });

    await cas.log("The client requires DPoP-bound access tokens, so no grant is accepted without a DPoP proof");
    await cas.doPost(accessTokenUrl, "grant_type=client_credentials&client_id=client&client_secret=secret&scope=openid", {}, () => {
        throw "Client credentials request without a DPoP proof must fail";
    }, (error) => {
        assert.equal(error.response.status, 400);
        assert(error.response.data.error === "invalid_dpop_proof");
    });

    let accessToken = null;
    await cas.doPost(accessTokenUrl, params, {
        "DPoP": dpopProof
    }, (res) => {
        accessToken = res.data.access_token;

        assert(accessToken !== undefined);
        assert(res.data.token_type === "DPoP");

        cas.decodeJwt(accessToken, true)
            .then((decoded) => {
                assert(decoded !== undefined);
                cas.log(decoded);
                assert(decoded.payload["DPoP"] !== undefined);
                assert(decoded.payload["DPoPConfirmation"] !== undefined);
                assert(decoded.payload["cnf"]["jkt"] !== undefined);
            });
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const sha256 = await cas.sha256(accessToken);
    const base64Token = await cas.base64Url(sha256);
    const profilePayload = {
        "htm": "POST",
        "htu": "https://localhost:8443/cas/oidc/profile",
        "iat": dt.getTime() / 1000,
        "jti": await cas.randomWord(16, false),
        "ath": base64Token
    };

    await cas.log("DPoP proof profile payload is");
    await cas.log(profilePayload);

    const dpopProofProfile = await cas.createJwt(profilePayload, privateKeyPem, "ES256",
        {
            header: {
                jwk: publicJwk,
                typ: "dpop+jwt"
            }
        });
    await cas.log(`DPoP JWT is ${dpopProofProfile}`);

    const profileUrl = "https://localhost:8443/cas/oidc/profile";
    await cas.log("A DPoP-bound access token cannot be presented as a bearer token");
    await cas.doPost(`${profileUrl}?token=${accessToken}`, "", {
        "Content-Type": "application/json",
        "DPoP": dpopProofProfile
    }, () => {
        throw "DPoP-bound access token presented as a bearer token must be rejected";
    }, (error) => {
        assert.equal(error.response.status, 401);
        assert(error.response.data.error === "invalid_token");
    });

    await cas.log("A DPoP-bound access token presented without a DPoP proof is challenged");
    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `DPoP ${accessToken}`
    }, () => {
        throw "DPoP-bound access token presented without a DPoP proof must be rejected";
    }, (error) => {
        assert.equal(error.response.status, 401);
        assert(error.response.data.error === "invalid_dpop_proof");
        const challenge = error.response.headers["www-authenticate"];
        assert(challenge.startsWith("DPoP error=\"invalid_dpop_proof\""), challenge);
        assert(challenge.includes("algs=\""), challenge);
    });

    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `DPoP ${accessToken}`,
        "DPoP": dpopProofProfile
    }, (res) => {
        cas.log(res.data);
        assert(res.data.sub !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await introspect(accessToken);
    await cas.closeBrowser(browser);
})();

async function introspect(token) {
    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    await cas.log(`Introspecting token ${token}`);
    await cas.doGet(`https://localhost:8443/cas/oidc/introspect?token=${token}`,
        (res) => {
            assert(res.data.active === true);
            assert(res.data.tokenType === "DPoP");
            assert(res.data.cnf.jkt !== undefined);
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
}
