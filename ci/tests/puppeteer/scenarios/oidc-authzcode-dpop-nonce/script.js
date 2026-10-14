const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

const tokenUrl = "https://localhost:8443/cas/oidc/token";
const profileUrl = "https://localhost:8443/cas/oidc/profile";

async function buildProof(privateKeyPem, publicJwk, htu, nonce = undefined, accessToken = undefined) {
    const payload = {
        "htm": "POST",
        "htu": htu,
        "iat": Math.floor(Date.now() / 1000) - 5,
        "jti": await cas.randomWord(16, false)
    };
    if (nonce !== undefined) {
        payload.nonce = nonce;
    }
    if (accessToken !== undefined) {
        payload.ath = await cas.base64Url(await cas.sha256(accessToken));
    }
    return cas.createJwt(payload, privateKeyPem, "ES256", {header: {jwk: publicJwk, typ: "dpop+jwt"}});
}

async function expectNonceRequired(url, params, headers, status) {
    let nonce = undefined;
    await cas.doPost(url, params, headers, () => {
        throw "Request without a valid DPoP nonce must fail";
    }, (error) => {
        assert.equal(error.response.status, status);
        assert(error.response.data.error === "use_dpop_nonce");
        nonce = error.response.headers["dpop-nonce"];
        assert(nonce !== undefined);
        if (status === 401) {
            assert(error.response.headers["www-authenticate"].includes("use_dpop_nonce"));
        }
    });
    await cas.log(`DPoP nonce is ${nonce}`);
    return nonce;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const scopes = `${encodeURIComponent("openid profile")}`;
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${scopes}&redirect_uri=${redirectUrl}`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    const code = await cas.assertParameter(page, "code");

    const {publicKey, privateKey} = await jose.generateKeyPair("ES256", {extractable: true});
    const publicJwk = await jose.exportJWK(publicKey);
    const privateKeyPem = await jose.exportPKCS8(privateKey);

    const params = `grant_type=authorization_code&client_id=client&client_secret=secret&redirect_uri=${redirectUrl}&code=${code}`;
    const nonce = await expectNonceRequired(tokenUrl, params,
        {"DPoP": await buildProof(privateKeyPem, publicJwk, tokenUrl)}, 400);

    let accessToken = undefined;
    await cas.doPost(tokenUrl, params, {"DPoP": await buildProof(privateKeyPem, publicJwk, tokenUrl, nonce)}, (res) => {
        accessToken = res.data.access_token;
        assert(accessToken !== undefined);
        assert(res.data.token_type === "DPoP");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const authorization = `DPoP ${accessToken}`;
    const profileNonce = await expectNonceRequired(profileUrl, "", {
        "Authorization": authorization,
        "DPoP": await buildProof(privateKeyPem, publicJwk, profileUrl, undefined, accessToken)
    }, 401);
    await cas.doPost(profileUrl, "", {
        "Authorization": authorization,
        "DPoP": await buildProof(privateKeyPem, publicJwk, profileUrl, profileNonce, accessToken)
    }, (res) => {
        assert(res.data.sub !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
    await cas.closeBrowser(browser);
})();
