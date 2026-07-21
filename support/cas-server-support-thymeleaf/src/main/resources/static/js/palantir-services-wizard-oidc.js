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

    createRegisteredServiceClientSecretsField("editServiceWizardOAuthOidcContainer");

    const features = await fetchCasFeatures();
    if (features.includes("OpenIDConnect")) {
        await $.get(`${PalantirDashboardConfiguration.casServerPrefix()}/oidc/.well-known/openid-configuration`, response => {
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

function createRegisteredServiceClientSecretsField(containerId) {
    const sectionId = "registeredServiceClientSecretsContainer";
    const rowsId = "registeredServiceClientSecretsRows";
    const addButtonId = "registeredServiceClientSecretsAddButton";
    const hiddenId = "registeredServiceClientSecrets";

    const html = `
        <section id="${sectionId}" class="registered-service-client-secrets mb-2">
            <h3 class="mt-2 mb-2">Client Secrets</h3>
            <input type="hidden" id="${hiddenId}" value="clientSecrets" data-param-name="clientSecrets"/>
            <div id="${rowsId}"></div>
            <button type="button"
                    id="${addButtonId}"
                    title="Add Client Secret"
                    class="mdc-button mdc-button--raised mdc-button--round add-row mt-2">
                <span class="mdc-button__label">
                    <i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>
                </span>
            </button>
        </section>`;
    $(`#${containerId}`).append($(html));

    $(`#${hiddenId}`)
        .data("renderer", () => buildRegisteredServiceClientSecretsDefinition())
        .data("beforeGenerate", function ($input, serviceDefinition) {
            const definition = serviceDefinition.clientSecrets;
            if (definition && typeof definition === "object" && !Array.isArray(definition) && Object.keys(definition).length === 0) {
                delete serviceDefinition.clientSecrets;
                return;
            }
            if (definition && definition.legacyClientSecret) {
                serviceDefinition.clientSecret = definition.legacyClientSecret;
                delete serviceDefinition.clientSecrets;
            }
        });

    $(`#${addButtonId}`).on("click", () => addRegisteredServiceClientSecretRow());
    addRegisteredServiceClientSecretRow();
}

function createClientSecretTextField({id, label, type = "text", cssClasses = "", title = ""}) {
    return `
        <label for="${id}"
               class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label mb-2 ${cssClasses}">
            <span class="mdc-notched-outline">
                <span class="mdc-notched-outline__leading"></span>
                <span class="mdc-notched-outline__notch">
                    <span class="mdc-floating-label">${label}</span>
                </span>
                <span class="mdc-notched-outline__trailing"></span>
            </span>
            <input class="mdc-text-field__input form-control ${cssClasses}"
                   id="${id}"
                   name="${id}"
                   type="${type}"
                   title="${title}"
                   autocomplete="off"/>
        </label>`;
}

function initializeRegisteredServiceClientSecretDatePicker($input) {
    $input.datepicker({
        dateFormat: "mm/dd/yy",
        showAnim: "slideDown",
        onSelect: function () {
            generateServiceDefinition();
            cas.attachFields("#registeredServiceClientSecretsContainer");
        }
    });
}

let registeredServiceClientSecretRowCounter = 0;

function addRegisteredServiceClientSecretRow(secret = {}) {
    const rowIndex = registeredServiceClientSecretRowCounter;
    registeredServiceClientSecretRowCounter++;
    const rowId = `registeredServiceClientSecretRow${rowIndex}`;
    const secretId = `registeredServiceClientSecretValue${rowIndex}`;
    const expirationDateId = `registeredServiceClientSecretExpirationDate${rowIndex}`;
    const removeButtonId = `registeredServiceClientSecretRemove${rowIndex}`;
    const generateButtonId = `registeredServiceClientSecretGenerate${rowIndex}`;

    const row = $(`
        <div class="registered-service-client-secret-row d-flex align-items-start justify-content-between pt-2" id="${rowId}">
            ${createClientSecretTextField({
                id: secretId,
                label: "Client Secret",
                type: "password",
                cssClasses: "registered-service-client-secret-value",
                title: "Define a client secret for this OAuth/OpenID Connect relying party."
            })}
            <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2 registered-service-client-secret-action"
                    id="${generateButtonId}"
                    type="button"
                    title="Generate Client Secret">
                <i class="mdi mdi-refresh" aria-hidden="true"></i>
                <span class="sr-only">Generate</span>
            </button>
            ${createClientSecretTextField({
                id: expirationDateId,
                label: "Expiration Date",
                cssClasses: "jquery-datepicker registered-service-client-secret-expiration-date",
                title: "Select the client secret expiration date. Leave empty for no expiration."
            })}
            <button type="button"
                    id="${removeButtonId}"
                    title="Remove Client Secret"
                    class="mdc-button mdc-button--raised btn btn-link mdc-button--inline-row registered-service-client-secret-action">
                <i class="mdi mdi-minus-thick" aria-hidden="true"></i>
            </button>
        </div>`);

    decoratePalantirInputIcons(row[0]);
    $("#registeredServiceClientSecretsRows").append(row);

    const $secret = $(`#${secretId}`);
    const $expirationDate = $(`#${expirationDateId}`);
    $secret.val(secret.value || "");
    $expirationDate.val(secret.expirationDate || "");

    row.find("input").on("input change", () => generateServiceDefinition());
    $(`#${generateButtonId}`).on("click", () => {
        $secret.val(generateRandom()).focus();
        generateServiceDefinition();
        cas.attachFields("#registeredServiceClientSecretsContainer");
    });
    $(`#${removeButtonId}`).on("click", () => {
        row.remove();
        if ($(".registered-service-client-secret-row").length === 0) {
            addRegisteredServiceClientSecretRow();
        }
        generateServiceDefinition();
    });
    initializeRegisteredServiceClientSecretDatePicker($expirationDate);
    cas.attachFields("#registeredServiceClientSecretsContainer");
    generateServiceDefinition();
}

function buildRegisteredServiceClientSecretsDefinition() {
    const secrets = getRegisteredServiceClientSecretRows();
    if (secrets.length === 0) {
        return {};
    }
    if (secrets.length === 1 && !secrets[0].expiration) {
        return {legacyClientSecret: secrets[0].value};
    }
    return ["java.util.ArrayList", secrets.map(secret => {
        const record = {
            "@class": "org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret",
            value: secret.value
        };
        if (secret.expiration) {
            record.expiration = secret.expiration;
        }
        return record;
    })];
}

function getRegisteredServiceClientSecretRows() {
    return $(".registered-service-client-secret-row")
        .map(function () {
            const $row = $(this);
            const value = $row.find("input.registered-service-client-secret-value").val()?.trim();
            if (!value) {
                return null;
            }
            const expirationDate = $row.find("input.registered-service-client-secret-expiration-date").val()?.trim();
            return {
                value,
                expiration: expirationDate || ""
            };
        })
        .get()
        .filter(Boolean);
}

function resetRegisteredServiceClientSecrets() {
    registeredServiceClientSecretRowCounter = 0;
    $("#registeredServiceClientSecretsRows").empty();
    $("#registeredServiceClientSecrets").val("clientSecrets");
    addRegisteredServiceClientSecretRow();
}

function populateRegisteredServiceClientSecrets(serviceDefinition) {
    const secrets = extractRegisteredServiceClientSecrets(serviceDefinition);
    $("#registeredServiceClientSecretsRows").empty();
    if (secrets.length === 0) {
        addRegisteredServiceClientSecretRow();
        return;
    }
    secrets.forEach(secret => addRegisteredServiceClientSecretRow(secret));
}

function extractRegisteredServiceClientSecrets(serviceDefinition) {
    if (!serviceDefinition) {
        return [];
    }
    let secrets = [];
    if (Array.isArray(serviceDefinition.clientSecrets)) {
        secrets = serviceDefinition.clientSecrets.length === 2
            && typeof serviceDefinition.clientSecrets[0] === "string"
            && Array.isArray(serviceDefinition.clientSecrets[1])
            ? serviceDefinition.clientSecrets[1]
            : serviceDefinition.clientSecrets;
    } else if (serviceDefinition.clientSecret) {
        secrets = [{
            value: serviceDefinition.clientSecret,
            expiration: serviceDefinition.clientSecretExpiration || ""
        }];
    }
    return secrets
        .filter(secret => secret && secret.value)
        .map(secret => ({
            value: secret.value,
            ...parseRegisteredServiceClientSecretExpiration(secret.expiration)
        }));
}

function parseRegisteredServiceClientSecretExpiration(expiration) {
    if (!expiration) {
        return {expirationDate: ""};
    }
    const value = String(expiration).trim();
    if (/^\d+$/.test(value)) {
        return formatRegisteredServiceClientSecretExpirationDate(new Date(Number(value) * 1000));
    }
    const isoDate = new Date(value.endsWith("Z") ? value : `${value}Z`);
    if (!Number.isNaN(isoDate.getTime())) {
        return formatRegisteredServiceClientSecretExpirationDate(isoDate);
    }
    const match = value.match(/^(\d{1,2}\/\d{1,2}\/\d{4})(?:\s+(\d{1,2}):(\d{2})(?:\s*([AP]M))?)?$/i);
    if (match) {
        return {
            expirationDate: match[1]
        };
    }
    return {expirationDate: ""};
}

function formatRegisteredServiceClientSecretExpirationDate(date) {
    const month = String(date.getUTCMonth() + 1).padStart(2, "0");
    const day = String(date.getUTCDate()).padStart(2, "0");
    const year = date.getUTCFullYear();
    return {
        expirationDate: `${month}/${day}/${year}`
    };
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
