const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");
const jwkToPem = require("jwk-to-pem");

const key = JSON.parse(fs.readFileSync(path.join(__dirname, "/keystore.json"))).keys[0];
const privateKey = jwkToPem(key, {private: true});
const CODE_CHALLENGE = "cwr1RXW4wcqyi0Eq9h1tD2tliFRYf36HMqG0lumwCtE";
const CODE_VERIFIER = "zkuyfY0CcG1yuVojREYwtbnpjOsOleD.OWkBpNVTHKyABMJ0ly_ZKTeOi."
    + "STPvshXsHyShcyAzm6z4ThKr2Y91RKFLvmOkJEiBhaSzIp~YHH3wkrzlB6m~y8h~td_pPg";

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
            typ: "openid4vci-proof+jwt",
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
    const payload = await getPayload(page, url, "client", [
        {
            "type": "openid_credential",
            "credential_configuration_id": "myorg"
        }
    ], "openid");
    await cas.closeBrowser(browser);

    url = "https://localhost:8443/cas/oidc/oidcVcCredential";

    /**
     * The token response returned authorization details carrying credential identifiers, so
     * OpenID4VCI 1.0 requires the credential request to use credential_identifier rather than
     * credential_configuration_id.
     */
    const credentialIdentifiers = payload.authorization_details[0].credential_identifiers;
    assert(Array.isArray(credentialIdentifiers) && credentialIdentifiers.length > 0);

    const proof = await createPublicKey();
    const credentialRequest = JSON.stringify({
        credential_identifier: credentialIdentifiers[0],
        proofs: {
            jwt: [proof]
        }
    });
    await cas.log(`Calling ${url}`);
    const result = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${payload.access_token}`
    }, 200, credentialRequest));
    await cas.log(result);
    assert(result.credentials.length === 1);
    assert(result.credential === undefined, "OpenID4VCI 1.0 replaced credential with the credentials array");
    assert(result.format === undefined, "OpenID4VCI 1.0 carries no format in the credential response");

    const parts = result.credentials[0].credential.split("~");
    const issuerJwt = parts[0];
    const decoded = await cas.decodeJwt(issuerJwt);
    assert(decoded !== undefined && decoded !== null);

    assert(decoded.sub === "casuser");
    assert(decoded.email === "casuser@example.org");
    assert(decoded.given_name === "CAS");
    assert(decoded.family_name === "User");
    assert(decoded.score === 95.5);
    assert(decoded.roles.length === 2);
    assert(decoded.roles.includes("user"));
    assert(decoded.roles.includes("admin"));
    assert(decoded.student_id === undefined);

    await cas.log("Requesting the credential by its scope instead of authorization details");
    const scopedBrowser = await cas.newBrowser(cas.browserOptions());
    const scopedPage = await cas.newPage(scopedBrowser);
    const scopedPayload = await getPayload(scopedPage, "https://localhost:9859/anything/sample1",
        "client", undefined, "openid SystemCredential");
    await cas.closeBrowser(scopedBrowser);
    assert(scopedPayload.authorization_details === undefined);

    const scopedResult = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${scopedPayload.access_token}`
    }, 200, JSON.stringify({
        credential_configuration_id: "myorg",
        proofs: {
            jwt: [await createPublicKey()]
        }
    })));
    await cas.log(scopedResult);
    assert(scopedResult.credentials.length === 1);
    const scopedCredential = await cas.decodeJwt(scopedResult.credentials[0].credential.split("~")[0]);
    assert(scopedCredential.given_name === "CAS");

    await cas.log("Requesting the credential with authorization details sent through a pushed authorization request");
    const pushedBrowser = await cas.newBrowser(cas.browserOptions());
    const pushedPage = await cas.newPage(pushedBrowser);
    const pushedPayload = await getPayloadWithPushedAuthorizationRequest(pushedPage, "https://localhost:9859/anything/wallet", [
        {
            "type": "openid_credential",
            "credential_configuration_id": "myorg"
        }
    ]);
    await cas.closeBrowser(pushedBrowser);
    const pushedIdentifiers = pushedPayload.authorization_details[0].credential_identifiers;
    assert(Array.isArray(pushedIdentifiers) && pushedIdentifiers.length > 0);

    const pushedResult = JSON.parse(await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${pushedPayload.access_token}`
    }, 200, JSON.stringify({
        credential_identifier: pushedIdentifiers[0],
        proofs: {
            jwt: [await createPublicKey()]
        }
    })));
    await cas.log(pushedResult);
    assert(pushedResult.credentials.length === 1);

    await cas.log("Pushing authorization details for a credential the issuer does not offer is refused right away");
    const refused = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/oidcPushAuthorize", "POST", {
        "Content-Type": "application/x-www-form-urlencoded"
    }, 400, new URLSearchParams({
        response_type: "code",
        client_id: "wallet",
        client_secret: "secret",
        scope: "openid",
        redirect_uri: "https://localhost:9859/anything/wallet",
        authorization_details: JSON.stringify([{type: "openid_credential", credential_configuration_id: "unknown"}])
    }).toString()));
    assert.equal(refused.error, "invalid_authorization_details");
    assert(refused.request_uri === undefined);
})();

/**
 * A wallet that pushes its authorization request (RFC 9126) sends authorization_details with the pushed request,
 * and the authorization request that follows carries only the client id and the request URI.
 */
async function getPayloadWithPushedAuthorizationRequest(page, redirectUri, authorizationDetails) {
    const clientAuthentication = "client_id=wallet&client_secret=secret";
    const parameters = `response_type=code&${clientAuthentication}&scope=openid&redirect_uri=${redirectUri}`
        + `&authorization_details=${encodeURIComponent(JSON.stringify(authorizationDetails))}`
        + `&code_challenge=${CODE_CHALLENGE}&code_challenge_method=S256&issuer_state=abcdefg1234567890`;
    const requestUri = await cas.doPost(`https://localhost:8443/cas/oidc/oidcPushAuthorize?${parameters}`, "", {
        "Content-Type": "application/x-www-form-urlencoded"
    }, (res) => res.data.request_uri, (error) => {
        throw `Operation failed to push the authorization request: ${error}`;
    });
    assert(requestUri !== undefined && requestUri.length > 0);
    await cas.log(`Request URI ${requestUri}`);
    await cas.goto(page, `https://localhost:8443/cas/oidc/authorize?client_id=wallet&request_uri=${encodeURIComponent(requestUri)}`);
    return completeAuthorization(page, redirectUri, clientAuthentication);
}

async function getPayload(page, redirectUri, clientId, authorizationDetails, scope) {
    let url = "https://localhost:8443/cas/oidc/authorize";
    url += `?response_type=code&client_id=${clientId}&scope=${encodeURIComponent(scope)}&redirect_uri=${redirectUri}`;
    if (authorizationDetails !== undefined) {
        url += `&authorization_details=${encodeURIComponent(JSON.stringify(authorizationDetails))}`;
    }
    url += `&code_challenge=${CODE_CHALLENGE}&code_challenge_method=S256&issuer_state=abcdefg1234567890`;

    await cas.goto(page, url);
    return completeAuthorization(page, redirectUri, `client_id=${clientId}`);
}

async function completeAuthorization(page, redirectUri, clientAuthentication) {
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
        + `&${clientAuthentication}&redirect_uri=${redirectUri}&code=${code}&code_verifier=${CODE_VERIFIER}`;
    return cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}

