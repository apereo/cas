const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    await cas.doGet("https://localhost:8443/cas/actuator/discoveryProfile",
        res => {
            assert(res.status === 200)
            assert(res.data.profile.registeredServiceTypesSupported !== null);
            assert(res.data.profile.availableAuthenticationHandlers !== null);
            assert(res.data.profile.availableAttributes !== null);
            assert(res.data.profile.delegatedClientTypesSupported !== null);
            assert(res.data.profile.userDefinedScopes !== null);
            assert(res.data.profile.multifactorAuthenticationProviderTypesSupported !== null);
            assert(res.data.profile.ticketTypesSupported !== null);
        },
        error => {
            throw error;
        })
})();
