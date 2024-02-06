const assert = require("assert");
const cas = require("../../cas.js");

(async () =>
    cas.doGet("https://localhost:8443/cas/actuator/discoveryProfile",
        (res) => {
            assert(res.status === 200);
            assert(res.data.profile.registeredServiceTypesSupported !== undefined);
            assert(res.data.profile.availableAuthenticationHandlers !== undefined);
            assert(res.data.profile.availableAttributes !== undefined);
            assert(res.data.profile.multifactorAuthenticationProviderTypesSupported !== undefined);
            assert(res.data.profile.ticketTypesSupported !== undefined);

            assert(res.data.profile.details.delegatedClientTypesSupported !== undefined);
            assert(res.data.profile.details.userDefinedScopes !== undefined);

        },
        (error) => {
            throw error;
        }))();
