
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const service = {
        "application_type": "web",
        "default_acr_values": ["mfa-duo", "mfa-gauth"],
        "redirect_uris": ["https://apereo.github.io", "https://github.com/apereo/cas"],
        "client_name": "My Example",
        "client_name#ja-Japan-JP": "Japanese",
        "logo_uri": "https://apereo.github.io/logo.png",
        "policy_uri": "https://github.com/apereo/cas",
        "tos_uri": "https://client.example.org/tos",
        "subject_type": "pairwise",
        "token_endpoint_auth_method": "client_secret_basic",
        "jwks_uri": "https://client.example.org/my_public_keys.jwks",
        "id_token_signed_response_alg": "RS256",
        "id_token_encrypted_response_alg": "RSA1_5",
        "id_token_encrypted_response_enc": "A128CBC-HS256",
        "userinfo_encrypted_response_alg": "RSA1_5",
        "userinfo_encrypted_response_enc": "A128CBC-HS256",
        "contacts": ["sample@example.org", "user@example.org"]
    };

    const body = JSON.stringify(service, undefined, 2);
    await cas.log(`Sending ${body}`);
    const result = await cas.doRequest("https://localhost:8443/cas/oidc/register", "POST",
        {
            "Content-Length": body.length,
            "Content-Type": "application/json"
        }, 201, body);
    assert(result !== null);
    const entity = JSON.parse(result.toString());
    await cas.log(entity);
    assert(entity.client_id !== undefined);
    assert(entity.client_secret !== undefined);
    assert(entity.registration_access_token !== undefined);
    assert(entity.registration_client_uri !== undefined);
    assert(entity.contacts.length === 2);

    await cas.log("Fetching client configuration...");
    await cas.log("==================================");
    
    await cas.doGet(entity.registration_client_uri,
        (res) => {
            cas.log(`Registered entity: ${JSON.stringify(res.data)}`);
            assert(res.data.client_secret_expires_at > 0);
            assert(res.data.client_name === "My Example");
            assert(res.data.client_id === entity.client_id);
            assert(res.data.client_secret === entity.client_secret);
            assert(res.status === 200);
        }, (error) => {
            throw error;
        },
        {
            "Authorization": `Bearer ${entity.registration_access_token}`
        });
})();
