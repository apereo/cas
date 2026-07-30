const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});

async function createPublicKey() {
    const nonce = await cas.doPost("https://localhost:8443/cas/oidc/oidcVcNonce", "", {
        "Content-Type": "application/json"
    }, (res) => {
        cas.log(res.data);
        return res.data.c_nonce;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const publicJwk = {
        kty: key.kty,
        n: key.n,
        e: key.e,
        kid: key.kid,
        use: key.use,
        alg: key.alg
    };

    return cas.createJwt({
        "jti": "THJZGsQDP26OuwQn",
        "iss": "client",
        "nonce": nonce,
        "aud": "https://localhost:8443/cas/oidc"
    }, privateKey, "RS256", {
        header: {
            jwk: publicJwk
        }
    });
}

(async () => {

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    
    let url = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${url}`);
    const payload = await getPayload(page, url, "client", "secret");
    await cas.closeBrowser(browser);

    url = "https://localhost:8443/cas/oidc/oidcVcCredential";

    const proof = await createPublicKey();
    const credentialRequest = JSON.stringify({
        credential_configuration_id: payload.authorization_details[0].credential_configuration_id,
        proof: {
            proof_type: "jwt",
            jwt: proof
        }
    });
    await cas.log(`Calling ${url}`);
    const result = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${payload.access_token}`
    }, 200, credentialRequest));
    await cas.log(result);
    assert(result.credential !== undefined);
    assert(result.format === "vc+sd-jwt");

    const decoded = await cas.decodeJwt(result.credential);
    assert(decoded.sub === "casuser");
    assert(decoded.email === "casuser@example.org");
    assert(decoded.given_name === "CAS");
    assert(decoded.family_name === "User");
    assert(decoded.score === 95.5);
    assert(decoded.roles.length === 2);
    assert(decoded.roles.includes("user"));
    assert(decoded.roles.includes("admin"));
    assert(decoded.student_id === "S12345");
})();

async function getPayload(page, redirectUri, clientId) {
    const codeChallenge = "cwr1RXW4wcqyi0Eq9h1tD2tliFRYf36HMqG0lumwCtE";
    const codeVerifier = "zkuyfY0CcG1yuVojREYwtbnpjOsOleD.OWkBpNVTHKyABMJ0ly_ZKTeOi."
        + "STPvshXsHyShcyAzm6z4ThKr2Y91RKFLvmOkJEiBhaSzIp~YHH3wkrzlB6m~y8h~td_pPg";

    const authorization = [
        {
            "type": "openid_credential",
            "credential_configuration_id": "myorg"
        }
    ];

    let url = "https://localhost:8443/cas/oidc/authorize";
    url += `?response_type=code&client_id=${clientId}&scope=openid&redirect_uri=${redirectUri}`;
    url += `&authorization_details=${encodeURIComponent(JSON.stringify(authorization))}&issuer_state=abcdefg1234567890`;
    url += `&code_challenge=${codeChallenge}&code_challenge_method=S256&issuer_state=abcdefg1234567890`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);

    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=${clientId}&redirect_uri=${redirectUri}&code=${code}&code_verifier=${codeVerifier}`;
    return cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}

