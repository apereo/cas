const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const issuer = "https://localhost:8443/cas/oidc/custom/issuer/fawnoos";
    await cas.doGet(`${issuer}/.well-known/openid-configuration`,
        function (res) {
            let result = res.data;
            assert(result.jwks_uri.startsWith(issuer));
            assert(result.authorization_endpoint.startsWith(issuer));
            assert(result.token_endpoint.startsWith(issuer));
            assert(result.userinfo_endpoint.startsWith(issuer));
            assert(result.userinfo_endpoint.startsWith(issuer));
            assert(result.registration_endpoint.startsWith(issuer));
            assert(result.end_session_endpoint.startsWith(issuer));
            assert(result.introspection_endpoint.startsWith(issuer));
            assert(result.revocation_endpoint.startsWith(issuer));
        },
        function (error) {
            throw error;
        });
})();
