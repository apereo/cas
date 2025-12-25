function createRegisteredServiceResponseMode() {
    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardOAuthOidcContainer",
        labelTitle: "Response Mode:",
        paramName: "responseMode",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {value: "query", text: "QUERY"},
            {value: "query.jwt", text: "QUERY JWT"},
            {value: "fragment", text: "FRAGMENT"},
            {value: "fragment.jwt", text: "FRAGMENT JWT"},
            {value: "form_post", text: "FORM POST"},
            {value: "form_post.jwt", text: "FORM POST JWT"}
        ],
        helpText: "Specify the response mode for this OAuth/OpenID Connect relying party."
    });
}

function createRegisteredServiceUserProfileViewType() {
    createSelectField({
        containerId: "editServiceWizardOAuthOidcContainer",
        labelTitle: "User Profile View Type:",
        paramName: "userProfileViewType",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {value: "NESTED", text: "NESTED"},
            {value: "FLAT", text: "FLAT"}
        ],
        helpText: "Specify the user profile view type for this OAuth/OpenID Connect relying party."
    });
}

function createOidcRegisteredServiceTlsClientSettings() {
    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "TLS Client Authentication Subject DN",
        name: "registeredServiceTlsClientAuthSubjectDn",
        paramName: "tlsClientAuthSubjectDn",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTlsClientSettings",
        title: "Define the TLS client authentication subject DN for this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "TLS Client Authentication SAN DNS",
        name: "registeredServiceTlsClientAuthSanDns",
        paramName: "tlsClientAuthSanDns",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTlsClientSettings",
        title: "Define the TLS client authentication SAN DNS for this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "TLS Client Authentication SAN URI",
        name: "registeredServiceTlsClientAuthSanUri",
        paramName: "tlsClientAuthSanUri",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTlsClientSettings",
        title: "Define the TLS client authentication SAN URI for this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "TLS Client Authentication SAN IP",
        name: "registeredServiceTlsClientAuthSanIP",
        paramName: "tlsClientAuthSanIp",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTlsClientSettings",
        title: "Define the TLS client authentication SAN IP for this OAuth/OpenID Connect relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "TLS Client Authentication SAN Email",
        name: "registeredServiceTlsClientAuthSanEmail",
        paramName: "tlsClientAuthSanEmail",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTlsClientSettings",
        title: "Define the TLS client authentication SAN email for this OAuth/OpenID Connect relying party."
    });
}

function createRegisteredServiceCodeExpirationPolicy() {
    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthCodeExpirationPolicy",
        labelTitle: "Number of Uses",
        name: "registeredServiceOAuthCodeExpirationPolicyNumberOfUses",
        paramName: "codeExpirationPolicy.numberOfUses",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemOAuthOidcCodeExpirationPolicy",
        multipleValues: true,
        title: "Control the number of times the ticket can be used"
    });

    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthCodeExpirationPolicy",
        labelTitle: "Time to Live",
        name: "registeredServiceOAuthCodeExpirationPolicyTimeToLive",
        paramName: "codeExpirationPolicy.timeToLive",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcCodeExpirationPolicy",
        title: "Control how long the ticket should be valid, in seconds.",
        multipleValues: true
    });
}

function createRegisteredServiceDeviceTokenExpirationPolicy() {
    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy",
        labelTitle: "Time to Kill",
        name: "registeredServiceDTExpirationPolicyTimeToKill",
        paramName: "deviceTokenExpirationPolicy.timeToKill",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcDTExpirationPolicy",
        title: "Control how long the ticket should be kept alive"
    });
}

function createRegisteredServiceOidcTokenExchangePolicy() {
    createSelectField({
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        labelTitle: "Type:",
        paramName: "tokenExchangePolicy",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
                text: "IMPERSONATION"
            },
            {
                value: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
                text: "DELEGATION"
            }

        ],
        helpText: "Specifies the OAuth token exchange policy type.",
        changeEventHandlers: "handleRegisteredServiceTokenExchangePolicyChange"
    })
        .data("renderer", function (value) {
            return {"@class": value};
        });

    createInputField({
        cssClasses: "hide IMPERSONATION",
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
        labelTitle: "Allowed Resources (separated by comma)",
        name: "registeredServiceOAuthTokenExchangePolicyAllowedResources",
        paramName: "tokenExchangePolicy.allowedResources",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        title: "Define the resources allowed for token exchange, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
    });

    createInputField({
        cssClasses: "hide IMPERSONATION",
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
        labelTitle: "Allowed Audience(s) (separated by comma)",
        name: "registeredServiceOAuthTokenExchangePolicyAllowedAudiences",
        paramName: "tokenExchangePolicy.allowedAudience",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        title: "Define the audience allowed for token exchange, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
    });

    createInputField({
        cssClasses: "hide IMPERSONATION",
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
        labelTitle: "Allowed Token Types(s) (separated by comma)",
        name: "registeredServiceOAuthTokenExchangePolicyAllowedTokenTypes",
        paramName: "tokenExchangePolicy.allowedTokenTypes",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        title: "Define the token types allowed for token exchange, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
    });

    createInputField({
        cssClasses: "hide DELEGATION",
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
        labelTitle: "Allowed Actor Token Types(s) (separated by comma)",
        name: "registeredServiceOAuthTokenExchangePolicyAllowedActorTokenTypes",
        paramName: "tokenExchangePolicy.allowedActorTokenTypes",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        title: "Define the actor token types allowed for token exchange, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
    });

    createMappedInputField({
        header: "Required Actor Token Attributes",
        cssClasses: "hide DELEGATION",
        containerId: "editServiceWizardMenuItemOAuthOidcTokenExchangePolicy",
        keyField: "tokenExchangePolicyActorTokenAttributeName",
        keyLabel: "Attribute Name",
        valueField: "tokenExchangePolicyActorTokenAttributeValue",
        valueLabel: "Attribute Value Pattern",
        containerField: "tokenExchangePolicy.requiredActorTokenAttributes",
        multipleValues: true,
        multipleValuesType: "java.util.HashSet"
    });
}

function createRegisteredServiceOidcAccessTokenExpirationPolicy() {
    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy",
        labelTitle: "Time to Kill",
        name: "registeredServiceATExpirationPolicyTimeToKill",
        paramName: "accessTokenExpirationPolicy.timeToKill",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcATExpirationPolicy",
        title: "Control how long the ticket should be kept alive"
    });

    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy",
        labelTitle: "Maximum Time to Live",
        name: "registeredServiceATExpirationPolicyMaxTimeToLive",
        paramName: "accessTokenExpirationPolicy.maxTimeToLive",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcATExpirationPolicy",
        title: "Control how long (maximum) the ticket should be valid, in seconds."
    });

    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy",
        labelTitle: "Maximum Active Tokens",
        name: "registeredServiceATExpirationPolicyMaxActiveTokens",
        paramName: "accessTokenExpirationPolicy.maxActiveTokens",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemOAuthOidcATExpirationPolicy",
        title: "Control the maximum number of active access tokens."
    });
}

function createRegisteredServiceOidcRefreshTokenExpirationPolicy() {
    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy",
        labelTitle: "Time to Kill",
        name: "registeredServiceRTExpirationPolicyTimeToKill",
        paramName: "refreshTokenExpirationPolicy.timeToKill",
        required: false,
        containerId: "editServiceWizardMenuItemOAuthOidcRTExpirationPolicy",
        title: "Control how long the ticket should be kept alive"
    });

    createInputField({
        paramType: "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy",
        labelTitle: "Maximum Active Tokens",
        name: "registeredServiceRTExpirationPolicyMaxActiveTokens",
        paramName: "refreshTokenExpirationPolicy.maxActiveTokens",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemOAuthOidcRTExpirationPolicy",
        title: "Control the maximum number of active refresh tokens."
    });
}

function handleRegisteredServiceTokenExchangePolicyChange(select) {
    const type = $(select).find("option:selected").text();
    $(`#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy .${type}`).show();
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemOAuthOidcTokenExchangePolicy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
}
