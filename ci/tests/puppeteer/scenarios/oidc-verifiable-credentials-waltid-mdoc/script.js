const cas = require("../../cas.js");
const assert = require("assert");

/**
 * ISO/IEC 18013-5 mdocs (mso_mdoc) issued to and presented by the walt.id Wallet API v2, which implements the
 * finalized OpenID4VCI 1.0 and OpenID4VP 1.0 specifications. Authentication is off in the shipped configuration,
 * so a wallet is created and driven directly over HTTP.
 */
const WALLET_API = "http://localhost:7006";

const DOCTYPE = "org.iso.18013.5.1.mDL";

const NAMESPACE = "org.iso.18013.5.1";

async function walletRequest(path, method, body) {
    const payload = body === undefined ? undefined : JSON.stringify(body);
    const headers = {"Accept": "application/json"};
    if (payload !== undefined) {
        headers["Content-Type"] = "application/json";
        headers["Content-Length"] = payload.length;
    }
    let status = 0;
    const response = await cas.doRequest(`${WALLET_API}${path}`, method, headers, 0, payload,
        (res) => {
            status = res.statusCode;
        });
    assert(status >= 200 && status < 300,
        `${method} ${path} answered HTTP ${status}: ${response}`);
    return response === undefined || response === "" ? undefined : JSON.parse(response);
}

/**
 * The wallet's P-256 key becomes the mdoc's device key: CAS binds the mdoc to the key its proof is signed with, and an
 * mdoc can only bind an elliptic curve or EdDSA key.
 */
async function createWallet() {
    const wallet = await walletRequest("/wallet", "POST", {});
    assert(wallet.walletId !== undefined);
    const key = await walletRequest(`/wallet/${wallet.walletId}/keys/generate`, "POST",
        {backend: "jwk", keyType: "secp256r1"});
    assert(key.keyId !== undefined, "The wallet did not return the generated key");
    await cas.logg(`Created wallet ${wallet.walletId} with key ${key.keyId}`);
    return {walletId: wallet.walletId, keyId: key.keyId};
}

async function verifyIssuerMetadata() {
    const metadata = JSON.parse(await cas.doRequest(
        "https://localhost:8443/cas/oidc/.well-known/openid-credential-issuer", "GET", {"Accept": "application/json"}, 200));
    const configuration = metadata.credential_configurations_supported.mdl;
    await cas.log(configuration);
    assert(configuration.format === "mso_mdoc");
    assert(configuration.doctype === DOCTYPE);
    assert(configuration.cryptographic_binding_methods_supported.includes("cose_key"));
    assert(configuration.credential_signing_alg_values_supported.includes(-7));
    assert(configuration.credential_metadata.claims.some((claim) => claim.path[0] === NAMESPACE && claim.path[1] === "given_name"));
}

async function receiveMobileDrivingLicence(wallet) {
    const body = JSON.stringify({"principal": "casuser", "credentialConfigurationIds": ["mdl"]});
    const transaction = JSON.parse(
        await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcCredentialOfferTransactions?scope=openid", "POST",
            {
                "Authorization": `Basic ${btoa("wallet-client:wallet-secret")}`,
                "Content-Length": body.length,
                "Content-Type": "application/json"
            },
            200,
            body)
    );
    await cas.logg(`Credential offer request: ${transaction.credentialOfferLink}`);

    const received = await walletRequest(`/wallet/${wallet.walletId}/credentials/receive`, "POST",
        {offerUrl: transaction.credentialOfferLink, keyId: wallet.keyId});
    await cas.log(received);
    assert(Array.isArray(received.credentialIds) && received.credentialIds.length === 1,
        "The wallet must store the mdoc it received");

    const stored = await walletRequest(`/wallet/${wallet.walletId}/credentials`, "GET");
    const credential = stored.find((entry) => entry.id === received.credentialIds[0]);
    await cas.log(credential);
    assert(credential !== undefined, "The mdoc is missing from the wallet");
    assert(JSON.stringify(credential).includes("mso_mdoc"), "The wallet must hold the credential as an mdoc");
}

/**
 * The wallet answers with a DeviceResponse whose device signature covers the OpenID4VP session transcript of this
 * request: client_id, nonce, response_uri and, for direct_post.jwt, the thumbprint of the key it encrypts to.
 */
async function presentMobileDrivingLicence(wallet, responseMode) {
    await cas.logg(`Presenting the mdoc with response mode ${responseMode}`);
    const body = JSON.stringify({
        "response_mode": responseMode,
        "credentials": [
            {
                "id": "mdl",
                "format": "mso_mdoc",
                "doctype_value": DOCTYPE,
                "claims": [
                    {"path": [NAMESPACE, "given_name"]},
                    {"path": [NAMESPACE, "family_name"]}
                ]
            }
        ]
    });
    const presentation = JSON.parse(
        await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcPresentationRequest", "POST",
            {
                "Authorization": `Basic ${btoa("wallet-client:wallet-secret")}`,
                "Content-Length": body.length,
                "Content-Type": "application/json"
            },
            200,
            body)
    );
    await cas.log(presentation);
    const authorizationRequest = new URL(presentation.authorization_request).searchParams;
    const dcqlQuery = JSON.parse(authorizationRequest.get("dcql_query"));
    assert(dcqlQuery.credentials[0].meta.doctype_value === DOCTYPE);
    const clientMetadata = JSON.parse(authorizationRequest.get("client_metadata"));
    assert(clientMetadata.vp_formats_supported.mso_mdoc.issuerauth_alg_values.includes(-7));

    const result = await walletRequest(`/wallet/${wallet.walletId}/credentials/present`, "POST",
        {requestUrl: presentation.authorization_request, keyId: wallet.keyId});
    await cas.log(result);
    assert(result.transmission_success === true, "The wallet failed to deliver the presentation");
    assert(result.verifier_response.status === "verified",
        `CAS did not verify the presentation: ${JSON.stringify(result.verifier_response)}`);

    const outcome = JSON.parse(
        await cas.doRequest(
            `https://localhost:8443/cas/oidc/oidcVcPresentationResult?requestId=${presentation.request_id}`, "GET",
            {"Authorization": `Basic ${btoa("wallet-client:wallet-secret")}`}, 200)
    );
    await cas.log(outcome);
    assert(outcome.status === "verified");
    assert(outcome.claims.mdl[NAMESPACE].given_name === "CAS");
    assert(outcome.claims.mdl[NAMESPACE].family_name === "User");
}

(async () => {
    await verifyIssuerMetadata();
    const wallet = await createWallet();
    await receiveMobileDrivingLicence(wallet);
    await cas.separator();
    await presentMobileDrivingLicence(wallet, "direct_post");
    await cas.separator();
    await presentMobileDrivingLicence(wallet, "direct_post.jwt");
})();
