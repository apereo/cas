function createOidcRegisteredServiceFields() {
    const $registeredServiceUsernameAttributeProvider = $("#registeredServiceUsernameAttributeProvider");
    let value = "org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider";
    if ($registeredServiceUsernameAttributeProvider.find(`option[value="${value}"]`).length === 0) {
        appendOptionsToDropDown({
            selectElement: $registeredServiceUsernameAttributeProvider,
            options: [{
                value: value,
                text: "PAIRWISE",
                data: {
                    hideDefaults: true,
                    markerClass: true,
                    serviceClass: "OidcRegisteredService"
                }
            }]
        });
    }
    $registeredServiceUsernameAttributeProvider.selectmenu("refresh");
}

async function createRegisteredServiceOidcFields() {
    createInputField({
        labelTitle: "Client ID",
        name: "registeredServiceClientId",
        paramName: "clientId",
        required: true,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the client identifier for this OAuth/OpenID Connect relying party."
    }).after(`
        <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="$('#registeredServiceClientId').val(generateRandom()).focus();generateServiceDefinition()">
            <i class="mdi mdi-refresh" aria-hidden="true"></i>
            <span class="sr-only">Generate</span>
        </button>
    `);

    createInputField({
        labelTitle: "Client Secret",
        name: "registeredServiceClientSecret",
        paramName: "clientSecret",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the client secret for this OAuth/OpenID Connect relying party."
    })
    .attr("type", "password")
    .after(`
        <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                onclick="$('#registeredServiceClientSecret').val(generateRandom()).focus(); generateServiceDefinition();"
                type="button">
            <i class="mdi mdi-refresh" aria-hidden="true"></i>
            <span class="sr-only">Generate</span>
        </button>
    `);


    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Client Secret Expiration",
        name: "registeredServiceClientSecretExpiration",
        paramName: "clientSecretExpiration",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardOAuthOidcContainer",
        serviceClass: "class-OidcRegisteredService",
        title: "Time, measured in UTC epoch, at which the client secret will expire or 0 if it will not expire."
    });

    const features = await fetchCasFeatures();
    if (features.includes("OpenIDConnect")) {
        await $.get(`${casServerPrefix}/oidc/.well-known/openid-configuration`, response => {
            const supportedScopes = response.scopes_supported.map(scope => ({value: scope, text: scope.toUpperCase()}));
            const supportedGrantTypes = response.grant_types_supported.map(scope => ({
                value: scope,
                text: scope.toUpperCase()
            }));
            const supportedResponseTypes = response.response_types_supported.map(scope => ({
                value: scope,
                text: scope.toUpperCase()
            }));

            createMultiSelectField({
                containerId: "editServiceWizardOAuthOidcContainer",
                labelTitle: "Scope(s):",
                paramName: "scopes",
                title: "Define the scope(s) for this OAuth/OpenID Connect relying party.",
                options: supportedScopes
            })
                .data("renderer", function (value) {
                    return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
                });

            createMultiSelectField({
                cssClasses: "advanced-option",
                containerId: "editServiceWizardOAuthOidcContainer",
                labelTitle: "Grant Type(s):",
                paramName: "supportedGrantTypes",
                title: "Define the supported grant type(s) for this OAuth/OpenID Connect relying party, separated by comma (e.g., <code>authorization_code,refresh_token,client_credentials</code>).",
                options: supportedGrantTypes
            })
                .data("renderer", function (value) {
                    return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
                });

            createMultiSelectField({
                cssClasses: "advanced-option",
                containerId: "editServiceWizardOAuthOidcContainer",
                labelTitle: "Response Type(s):",
                paramName: "supportedResponseTypes",
                title: "Define the supported response type(s) for this OAuth/OpenID Connect relying party, separated by comma (e.g., <code>code,id_token,token</code>).",
                options: supportedResponseTypes
            })
                .data("renderer", function (value) {
                    return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
                });
        });
    }


    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Audience(s)",
        name: "registeredServiceAudience",
        paramName: "audience",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the audience(s) for this OAuth/OpenID Connect relying party, separated by comma."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "JWKS",
        name: "registeredServiceJwksUrl",
        paramName: "jwks",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the JSON Web Key Set (JWKS) URL for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "JWKS Key ID",
        name: "registeredServiceJwksKeyId",
        paramName: "jwksKeyId",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the JSON Web Key Set (JWKS) Key ID in the keystore for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "JWKS Cache Duration",
        name: "registeredServiceCacheDuration",
        paramName: "jwksCacheDuration",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the cache duration <code>PT5S</code> for the JSON Web Key Set (JWKS) for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "ID Token Issuer",
        name: "registeredServiceIdTokenIssuer",
        paramName: "idTokenIssuer",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the ID token issuer for this OpenID Connect relying party. Override the <code>iss</code> claim in the ID Token, which should only be used in special circumstances. Do NOT use this setting carelessly as the ID token’s issuer MUST ALWAYS match the identity provider’s issuer."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "ID Token Encryption Algorithm",
        name: "registeredServiceIdTokenEncryptionAlg",
        paramName: "idTokenEncryptionAlg",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the ID token encryption algorithm for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "ID Token Encryption Encoding",
        name: "registeredServiceIdTokenEncryptionEncoding",
        paramName: "idTokenEncryptionEncoding",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the ID token encryption encoding for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "ID Token Signing Algorithm",
        name: "registeredServiceIdTokenSigningAlg",
        paramName: "idTokenSigningAlg",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the ID token signing algorithm for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "User Info Signing Algorithm",
        name: "registeredServiceUserInfoSigningAlg",
        paramName: "userInfoSigningAlg",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the user info signing algorithm for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "User Info Encryption Response Algorithm",
        name: "registeredServiceUserInfoEncryptionResponseAlg",
        paramName: "userInfoEncryptedResponseAlg",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the user info encryption response algorithm for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "User Info Encryption Response Encoding",
        name: "registeredServiceUserInfoEncryptionResponseEncoding",
        paramName: "userInfoEncryptedResponseEncoding",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the user info encryption response encoding for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "JWT Access Token Signing Algorithm",
        name: "registeredServiceJwtAccessTokenSigningAlg",
        paramName: "jwtAccessTokenSigningAlg",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the JWT signing algorithm for access tokens issued to this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Introspection Response Signing Algorithm",
        name: "registeredServiceIntrospectionSignedResponseAlg",
        paramName: "introspectionSignedResponseAlg",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the signing algorithm for introspection responses issued to this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Introspection Response Encryption Algorithm",
        name: "registeredServiceIntrospectionEncryptedResponseAlg",
        paramName: "introspectionEncryptedResponseAlg",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the encryption algorithm for introspection responses issued to this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Introspection Response Encryption Encoding",
        name: "registeredServiceIntrospectionEncryptedResponseEncoding",
        paramName: "introspectionEncryptedResponseEncoding",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the encryption encoding for introspection responses issued to this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Token Endpoint Authentication Method",
        name: "registeredServiceTokenEndpointAuthenticationMethod",
        paramName: "tokenEndpointAuthenticationMethod",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the token endpoint authentication method for this OAuth/OpenID Connect relying party. Examples include <code>client_secret_basic,client_secret_jwt,etc</code>"
    });


    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Application Type",
        name: "registeredServiceApplicationType",
        paramName: "applicationType",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        serviceClass: "class-OidcRegisteredService",
        title: "Define the application type for this OpenID Connect relying party (e.g., <code>web, native, user_agent</code>)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Subject Type",
        name: "registeredServiceSubjectType",
        paramName: "subjectType",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        serviceClass: "class-OidcRegisteredService",
        title: "Define the subject type for this OpenID Connect relying party (e.g., <code>public,pairwise</code>)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Sector Identifier URI",
        name: "registeredServiceSectorIdentifierUri",
        paramName: "sectorIdentifierUri",
        required: false,
        containerId: "editServiceWizardOAuthOidcContainer",
        serviceClass: "class-OidcRegisteredService",
        title: "Define the sector identifier URI for this OpenID Connect relying party."
    });

    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardOAuthOidcContainer",
        labelTitle: "Backchannel Token Delivery Mode:",
        paramName: "backchannelTokenDeliveryMode",
        serviceClass: "class-OidcRegisteredService",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {value: "ping", text: "PING"},
            {value: "push", text: "PUSH"},
            {value: "pull", text: "POLL"}
        ],
        helpText: "Specify the backchannel token delivery mode for this OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Backchannel Client Notification Endpoint",
        name: "registeredServiceBackchannelClientNotificationEndpoint",
        paramName: "backchannelClientNotificationEndpoint",
        required: false,
        serviceClass: "class-OidcRegisteredService",
        containerId: "editServiceWizardOAuthOidcContainer",
        title: "Define the backchannel client notification endpoint for this OpenID Connect relying party."
    });
}

function createRegisteredServiceIdTokenExpirationPolicy() {
    createInputField({
        paramType: "org.apereo.cas.oidc.services.DefaultRegisteredServiceOidcIdTokenExpirationPolicy",
        labelTitle: "Time to Kill",
        name: "registeredServiceITExpirationPolicyTimeToKill",
        paramName: "idTokenExpirationPolicy.timeToKill",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcITExpirationPolicy",
        title: "Control how long the ticket should be kept alive"
    });
}
