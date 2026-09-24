const cas = require("../../cas.js");
const assert = require("assert");

/**
 * walt.id Wallet API v2 implements the finalized OpenID4VCI 1.0 and OpenID4VP 1.0
 * specifications; the original Wallet API only ever spoke the drafts. Authentication is off in
 * the shipped configuration, so a wallet is created and driven directly over HTTP.
 */
const WALLET_API = "http://localhost:7006";

const CAS_ISSUER = "http://host.docker.internal:8080/cas/oidc";

/**
 * The wallet answers with whichever 2xx suits the operation -- 201 for a created wallet, 200
 * elsewhere -- so the status is asserted as a class rather than pinned per endpoint, and the
 * body is reported when it is not a success.
 */
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

async function createWallet() {
    const wallet = await walletRequest("/wallet", "POST", {});
    assert(wallet.walletId !== undefined);
    await cas.logg(`Created wallet ${wallet.walletId}`);

    /**
     * A new wallet has empty stores and no key of its own, so one has to be generated before it
     * can prove possession. It must be a P-256 key: CAS advertises jwk cryptographic binding,
     * and when the credential is presented back it only accepts ES256/384/512 key binding JWTs.
     * secp256r1 is what walt.id calls P-256, and is also its own default.
     *
     * No DID is created. CAS binds credentials to a raw JWK, which is what
     * cryptographic_binding_methods_supported advertises, so the wallet has no reason to reach
     * for a DID-based proof.
     */
    const key = await walletRequest(`/wallet/${wallet.walletId}/keys/generate`, "POST",
        {backend: "jwk", keyType: "secp256r1"});
    assert(key.keyId !== undefined, "The wallet did not return the generated key");
    await cas.logg(`Generated wallet key ${key.keyId} of type ${key.keyType}`);

    const details = await walletRequest(`/wallet/${wallet.walletId}`, "GET");
    await cas.log(details);
    assert(details.keyStoreCount >= 1);

    return {walletId: wallet.walletId, keyId: key.keyId};
}

async function createVerifiableCredentialTransaction(credentialConfigurationIds) {
    await cas.log(`Creating verifiable credential transaction for ${credentialConfigurationIds}`);

    const body = JSON.stringify({
        "principal": "casuser",
        "credentialConfigurationIds": credentialConfigurationIds
    });
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
    assert(transaction.transactionId !== undefined);
    assert(transaction.credentialOfferUri !== undefined);
    return transaction;
}

async function startVerifiableCredentialFlowForConfiguration(wallet, ...configurationIds) {
    await cas.logg(`Starting verifiable credential flow for ${configurationIds}`);

    const transaction = await createVerifiableCredentialTransaction(configurationIds);
    const offerUrl =
        `openid-credential-offer://?${new URLSearchParams({
            credential_offer_uri: transaction.credentialOfferUri
        })}`;
    await cas.logg(`Credential offer request: ${offerUrl}`);

    /**
     * One call resolves the offer, redeems the pre-authorized code, fetches a nonce, signs the
     * proof of possession and stores what CAS returns in the OpenID4VCI 1.0 credentials array.
     */
    const received = await walletRequest(`/wallet/${wallet.walletId}/credentials/receive`, "POST",
        {offerUrl: offerUrl, keyId: wallet.keyId});
    await cas.log(received);
    assert(Array.isArray(received.credentialIds), "The wallet must report the credentials it stored");
    assert(received.credentialIds.length === configurationIds.length,
        `Expected ${configurationIds.length} credential(s) but the wallet stored ${received.credentialIds.length}`);

    const stored = await walletRequest(`/wallet/${wallet.walletId}/credentials`, "GET");
    await cas.log(stored);
    for (const credentialId of received.credentialIds) {
        const credential = stored.find((entry) => entry.id === credentialId);
        assert(credential !== undefined, `Credential ${credentialId} is missing from the wallet`);
        assert(credential.issuer !== undefined);
        await cas.log(`Wallet holds credential ${credential.id} in format ${credential.format}`);
    }
    return received.credentialIds;
}

async function startVerifiableCredentialPresentationFlow(wallet) {
    await cas.logg("Starting verifiable credential presentation flow");

    const credentialRequest = {
        "credentials": [
            {
                "id": "myorg",
                "format": "dc+sd-jwt",
                "vct_values": [
                    `${CAS_ISSUER}/oidcVcCredentialType/myorg`
                ],
                "claims": [
                    {"path": ["given_name"], "required": true},
                    {"path": ["family_name"], "required": true},
                    {"path": ["email"], "required": true},
                    {"path": ["roles"], "required": true}
                ]
            }
        ]
    };

    const body = JSON.stringify(credentialRequest);
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
    assert(presentation.request_id !== undefined);
    assert(presentation.expires_in > 0);

    /**
     * OpenID4VP 1.0 does not allow a request made under the redirect_uri client identifier
     * prefix to be signed, and a request URI must serve a signed request object. CAS therefore
     * passes the whole authorization request by value and offers no request URI.
     */
    assert(presentation.request_uri === undefined);
    assert(presentation.authorization_request.startsWith("openid4vp://authorize?"));
    const authorizationRequest = new URL(presentation.authorization_request).searchParams;
    const responseUri = authorizationRequest.get("response_uri");
    assert(responseUri.endsWith("/oidcVcPresentationResponse"));
    assert(authorizationRequest.get("client_id") === `redirect_uri:${responseUri}`);
    assert(authorizationRequest.get("response_type") === "vp_token");
    assert(authorizationRequest.get("response_mode") === "direct_post");
    assert(authorizationRequest.get("nonce") !== null);
    assert(authorizationRequest.get("state") === presentation.request_id);
    const dcqlQuery = JSON.parse(authorizationRequest.get("dcql_query"));
    assert(dcqlQuery.credentials.length === 1);
    assert(dcqlQuery.credentials[0].format === "dc+sd-jwt");
    const clientMetadata = JSON.parse(authorizationRequest.get("client_metadata"));
    assert(clientMetadata.vp_formats_supported["dc+sd-jwt"] !== undefined);

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
    assert(outcome.claims.myorg.given_name === "CAS");
    assert(outcome.claims.myorg.family_name === "User");
}

(async () => {
    const wallet = await createWallet();
    await startVerifiableCredentialFlowForConfiguration(wallet, "myorg");
    await cas.separator();
    await startVerifiableCredentialPresentationFlow(wallet);
    await cas.separator();
    await startVerifiableCredentialFlowForConfiguration(wallet, "employee");
    await cas.separator();
    await startVerifiableCredentialFlowForConfiguration(wallet, "myorg", "employee");
    await cas.separator();
})();
