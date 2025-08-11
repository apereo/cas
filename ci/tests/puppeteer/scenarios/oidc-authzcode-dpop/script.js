
const cas = require("../../cas.js");
const assert = require("assert");
const jose = require("jose");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUrl = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid profile")}&redirect_uri=${redirectUrl}`;

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
        "jti": "vqv2EAaJECl67LmE"
    };
    await cas.log("DPoP proof payload is");
    await cas.log(payload);

    const {publicKey, privateKey} = await jose.generateKeyPair("ES256", { extractable: true });
    const publicJwk = await jose.exportJWK(publicKey);
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
    const params = `grant_type=authorization_code&client_id=client&redirect_uri=${redirectUrl}&code=${code}`;

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
        "jti": "vqv2EAaJECl67LmE",
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

    const profileUrl = `https://localhost:8443/cas/oidc/profile?token=${accessToken}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json",
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
