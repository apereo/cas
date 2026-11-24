/* The wizard's sections for multifactor policies, service matching, access strategies and authentication policies. */

function createRegisteredServiceMultifactorPolicy() {
    if (!CasActuatorEndpoints.discoveryProfile()) {
        createInputField({
            labelTitle: "Multifactor Providers",
            name: "registeredServiceMfaProviders",
            paramName: "multifactorPolicy.multifactorAuthenticationProviders",
            paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
            required: false,
            containerId: "editServiceWizardMenuItemMfaPolicy",
            title: "Define the multifactor authentication providers to be used, separated by comma."
        })
            .data("renderer", function (value) {
                return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
            });
    } else {
        CasDiscoveryProfile.fetchIfNeeded()
            .always(async () => {
                const options = Object.entries(CasDiscoveryProfile.multifactorAuthenticationProviders()).map(([key, _value]) => ({
                    value: key,
                    text: _value
                }));
                createMultiSelectField({
                    containerId: "bypassEnabledSwitchButtonPanel",
                    labelTitle: "Multifactor Providers:",
                    paramName: "multifactorPolicy.multifactorAuthenticationProviders",
                    paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
                    title: "Define the multifactor authentication providers to be used, separated by comma.",
                    options: options,
                    allowCreateOption: options.length === 0,
                    inclusion: "before"
                }).data("renderer", function (value) {
                    return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
                });
            });
    }

    createSelectField({
        cssClasses: "advanced-option",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        containerId: "editServiceWizardMenuItemMfaPolicy",
        labelTitle: "Failure Mode:",
        paramName: "multifactorPolicy.failureMode",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "OPEN",
                text: "OPEN"
            },
            {
                value: "CLOSED",
                text: "CLOSED"
            },
            {
                value: "PHANTOM",
                text: "PHANTOM"
            },
            {
                value: "NONE",
                text: "NONE"
            }
        ],
        helpText: "Specifies the failure mode for multifactor authentication."
    });

    CasDiscoveryProfile.fetchIfNeeded()
        .always(async () => {
            const options = CasDiscoveryProfile.availableAttributes().map(attr => ({
                value: attr,
                text: attr
            }));

            createMultiSelectField({
                cssClasses: "hide advanced-option",
                singleSelect: true,
                containerId: "registeredServiceMultifactorPolicyfailureModeSelectContainer",
                labelTitle: "Principal Attribute Name Trigger:",
                paramName: "multifactorPolicy.principalAttributeNameTrigger",
                paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
                title: "Specifies the principal attribute name that triggers multifactor authentication.",
                options: options,
                allowCreateOption: true,
                inclusion: "after"
            });
        });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        cssClasses: "advanced-option",
        labelTitle: "Principal Attribute Value To Match",
        name: "registeredServiceMfaPrincipalAttributeValueToMatch",
        paramName: "multifactorPolicy.principalAttributeValueToMatch",
        required: false,
        containerId: "editServiceWizardMenuItemMfaPolicy",
        title: "Specifies the principal attribute value to match for triggering multifactor authentication."
    });

    createMappedInputField({
        cssClasses: "advanced-option",
        header: "Provider Selection Mappings",
        containerId: "editServiceWizardMenuItemMfaPolicy",
        keyField: "registeredServiceMfaProviderSelectionMappingProvider",
        keyLabel: "Provider ID",
        valueField: "registeredServiceMfaProviderSelectionMappingPattern",
        valueLabel: "Attribute Value Pattern",
        containerField: "multifactorPolicy.multifactorProviderSelectionMappings",
        containerType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        mapType: "java.util.LinkedHashMap"
    });


    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        cssClasses: "advanced-option",
        labelTitle: "Principal Attribute Value Bypass",
        name: "registeredServiceMfaBypassPrincipalAttributeValue",
        paramName: "multifactorPolicy.bypassPrincipalAttributeValue",
        required: false,
        containerId: "editServiceWizardMenuItemMfaPolicy",
        title: "Specifies the principal attribute value that bypasses multifactor authentication."
    });

    CasDiscoveryProfile.fetchIfNeeded()
        .always(async () => {
            const options = CasDiscoveryProfile.availableAttributes().map(attr => ({
                value: attr,
                text: attr
            }));
            
            createMultiSelectField({
                cssClasses: "hide advanced-option",
                singleSelect: true,
                containerId: "registeredServiceMfaBypassPrincipalAttributeValueFieldContainer",
                labelTitle: "Principal Attribute Name Bypass:",
                paramName: "multifactorPolicy.bypassPrincipalAttributeName",
                paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
                title: "Specifies the principal attribute name that bypasses multifactor authentication.",
                options: options,
                allowCreateOption: true,
                inclusion: "before"
            });
        });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        cssClasses: "advanced-option",
        labelTitle: "Request IP Pattern Bypass",
        name: "registeredServiceMfaBypassRequestIpPattern",
        paramName: "multifactorPolicy.bypassForRequestIpAddress",
        required: false,
        containerId: "editServiceWizardMenuItemMfaPolicy",
        title: "Specifies the request IP address pattern that bypasses multifactor authentication."
    }).after(`
       <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="validatePattern(this, '#registeredServiceMfaBypassRequestIpPattern')">
            <i class="mdi mdi-check-circle" aria-hidden="true"></i>
            <span class="sr-only">Validate</span>
        </button>
    `);

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
            cssClasses: "advanced-option",
            labelTitle: "Groovy Script",
            name: "registeredServiceMfaScript",
            paramName: "multifactorPolicy.script",
            required: false,
            containerId: "editServiceWizardMenuItemMfaPolicy",
            title: "Specifies the Groovy script location, inline or external, to determine multifactor authentication."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Multifactor Policy Groovy Script"
        });
    }
}

function handleMatchingStrategyChange(select) {
    const type = getLastWord($(select).val());
    $(`#editServiceWizardMenuItemMatchingStrategy .${type}`).show();
    $("#editServiceWizardMenuItemMatchingStrategy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemMatchingStrategy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemMatchingStrategy [id$='FieldContainer'] input").val("");
        resetWizardSwitches("#editServiceWizardMenuItemMatchingStrategy");
    }
}

function createRegisteredServiceMatchingStrategy() {
    createSelectField({
        containerId: "editServiceWizardMenuItemMatchingStrategy",
        labelTitle: "Type:",
        paramName: "matchingStrategy",
        options: [
            {
                value: "org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategy",
                text: "FULL REGEX",
                selected: true
            },
            {
                value: "org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy",
                text: "PARTIAL REGEX",
                data: {
                    markerClass: true
                }
            },
            {
                value: "org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategy",
                text: "LITERAL",
                data: {
                    markerClass: true
                }
            },
            {
                value: "org.apereo.cas.services.StartsWithRegisteredServiceMatchingStrategy",
                text: "STARTS WITH"
            }
        ],
        helpText: "Specifies the strategy to determine how service IDs are matched.",
        changeEventHandlers: "handleMatchingStrategyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide StartsWithRegisteredServiceMatchingStrategy",
        labelTitle: "Expected URL",
        name: "registeredServiceMatchingStrategyExpectedUrl",
        paramName: "matchingStrategy.expectedUrl",
        required: false,
        containerId: "editServiceWizardMenuItemMatchingStrategy",
        title: "Define the URL that the requested service must start with."
    });
}

function handleAccessStrategyChange(select) {
    const type = getLastWord($(select).val());
    if (typeof toggleAccessStrategyActivation === "function") {
        toggleAccessStrategyActivation(type);
    }
    $(`#editServiceWizardMenuItemAccessStrategy .${type}`).show();
    $("#editServiceWizardMenuItemAccessStrategy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAccessStrategy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAccessStrategy [id$='SelectContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAccessStrategy [id$='MapContainer']")
        .not(`.${type}`)
        .hide();

    // Only clear values when not populating from an existing service
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAccessStrategy [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemAccessStrategy [id$='MapContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemAccessStrategy");
    }
}

/**
 * The fields of the access strategies that ask an external authorization service, such as Cerbos, Permit.io or
 * Amazon Verified Permissions, and of the strategies that decide whether a surrogate session may access the
 * application.
 */
function createRegisteredServiceExternalAccessStrategies() {
    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "API URL",
        name: "registeredServiceAccessStrategyCerbosApiUrl",
        paramName: "accessStrategy.apiUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Cerbos policy decision point URL. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Token",
        name: "registeredServiceAccessStrategyCerbosToken",
        paramName: "accessStrategy.token",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the token used to call Cerbos. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Kind",
        name: "registeredServiceAccessStrategyCerbosKind",
        paramName: "accessStrategy.kind",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the kind of resource Cerbos checks. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Scope",
        name: "registeredServiceAccessStrategyCerbosScope",
        paramName: "accessStrategy.scope",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Cerbos policy scope. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Roles Attribute",
        name: "registeredServiceAccessStrategyCerbosRolesAttribute",
        paramName: "accessStrategy.rolesAttribute",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the attribute that holds the principal's roles. Defaults to <code>memberOf</code>. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Request ID",
        name: "registeredServiceAccessStrategyCerbosRequestId",
        paramName: "accessStrategy.requestId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the request identifier sent to Cerbos. Supports expression language."
    });

    createInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        labelTitle: "Actions (separated by comma)",
        name: "registeredServiceAccessStrategyCerbosActions",
        paramName: "accessStrategy.actions",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the actions Cerbos must allow, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.ArrayList", value.split(",").map(v => v.trim()).filter(v => v.length > 0)];
    });

    createMappedInputField({
        cssClasses: "hide CerbosRegisteredServiceAccessStrategy",
        header: "Auxiliary Data",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyCerbosAuxDataName",
        keyLabel: "Name",
        valueField: "registeredServiceAccessStrategyCerbosAuxDataValue",
        valueLabel: "Value",
        containerField: "accessStrategy.auxData"
    });

    createInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        labelTitle: "Access Key",
        name: "registeredServiceAccessStrategyAvpAccessKey",
        paramName: "accessStrategy.credentialAccessKey",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the AWS access key. Supports expression language."
    });

    createInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        labelTitle: "Secret Key",
        name: "registeredServiceAccessStrategyAvpSecretKey",
        paramName: "accessStrategy.credentialSecretKey",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the AWS secret key. Supports expression language."
    });

    createInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        labelTitle: "Region",
        name: "registeredServiceAccessStrategyAvpRegion",
        paramName: "accessStrategy.region",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the AWS region of the policy store. Supports expression language."
    });

    createInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        labelTitle: "Policy Store ID",
        name: "registeredServiceAccessStrategyAvpPolicyStoreId",
        paramName: "accessStrategy.policyStoreId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the policy store that holds the policies. Supports expression language."
    });

    createInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        labelTitle: "Action ID",
        name: "registeredServiceAccessStrategyAvpActionId",
        paramName: "accessStrategy.actionId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the action the policies authorize. Supports expression language."
    });

    createMappedInputField({
        cssClasses: "hide AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
        header: "Context",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyAvpContextName",
        keyLabel: "Name",
        valueField: "registeredServiceAccessStrategyAvpContextValue",
        valueLabel: "Value",
        containerField: "accessStrategy.context"
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "API Key",
        name: "registeredServiceAccessStrategyPermitApiKey",
        paramName: "accessStrategy.apiKey",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permit.io API key. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "PDP Address",
        name: "registeredServiceAccessStrategyPermitPdpAddress",
        paramName: "accessStrategy.pdpAddress",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permit.io policy decision point. Defaults to the Permit.io cloud PDP. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "Action",
        name: "registeredServiceAccessStrategyPermitAction",
        paramName: "accessStrategy.action",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the action Permit.io checks."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "Resource",
        name: "registeredServiceAccessStrategyPermitResource",
        paramName: "accessStrategy.resource",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the resource Permit.io checks."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "Tenant",
        name: "registeredServiceAccessStrategyPermitTenant",
        paramName: "accessStrategy.tenant",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permit.io tenant."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "Email Attribute",
        name: "registeredServiceAccessStrategyPermitEmailAttribute",
        paramName: "accessStrategy.emailAttributeName",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the attribute that holds the user's email. Defaults to <code>email</code>."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "First Name Attribute",
        name: "registeredServiceAccessStrategyPermitFirstNameAttribute",
        paramName: "accessStrategy.firstNameAttributeName",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the attribute that holds the user's first name. Defaults to <code>firstname</code>."
    });

    createInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        labelTitle: "Last Name Attribute",
        name: "registeredServiceAccessStrategyPermitLastNameAttribute",
        paramName: "accessStrategy.lastNameAttributeName",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the attribute that holds the user's last name. Defaults to <code>lastname</code>."
    });

    createMappedInputField({
        cssClasses: "hide PermitRegisteredServiceAccessStrategy",
        header: "Context",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyPermitContextName",
        keyLabel: "Name",
        valueField: "registeredServiceAccessStrategyPermitContextValue",
        valueLabel: "Value",
        containerField: "accessStrategy.context"
    });

    createMappedInputField({
        cssClasses: "hide SurrogateRegisteredServiceAccessStrategy",
        header: "Surrogate Required Attributes",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategySurrogateReqAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAccessStrategySurrogateReqAttrValue",
        valueLabel: "Attribute Values",
        containerField: "accessStrategy.surrogateRequiredAttributes",
        multipleValues: true,
        multipleValuesType: "java.util.HashSet"
    });
}

function createRegisteredServiceAccessStrategy() {
    createSelectField({
        cssClasses: "always-show",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        labelTitle: "Type:",
        paramName: "accessStrategy",
        options: [
            {
                value: "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
                text: "DEFAULT",
                selected: true
            },
            {
                value: "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy",
                text: "REMOTE"
            },
            {
                value: "org.apereo.cas.services.HttpRequestRegisteredServiceAccessStrategy",
                text: "HTTP REQUEST"
            },
            {
                value: "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
                text: "GROUPER"
            },
            {
                value: "org.apereo.cas.services.OpenFGARegisteredServiceAccessStrategy",
                text: "OPEN FGA"
            },
            {
                value: "org.apereo.cas.services.OpenPolicyAgentRegisteredServiceAccessStrategy",
                text: "OPA"
            },
            {
                value: "org.apereo.cas.services.PermifyRegisteredServiceAccessStrategy",
                text: "PERMIFY"
            },
            {
                value: "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy",
                text: "TIME BASED"
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy",
                        text: "GROOVY"
                    }]
                    : []
            ),
            {
                value: "org.apereo.cas.services.CerbosRegisteredServiceAccessStrategy",
                text: "CERBOS"
            },
            {
                value: "org.apereo.cas.permit.PermitRegisteredServiceAccessStrategy",
                text: "PERMIT.IO"
            },
            ...(
                CAS_FEATURES.some(feature => feature === "Core.aws")
                    ? [{
                        value: "org.apereo.cas.aws.authz.AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
                        text: "AMAZON VERIFIED PERMISSIONS"
                    }]
                    : []
            ),
            ...(
                CAS_FEATURES.some(feature => feature === "SurrogateAuthentication" || feature.startsWith("SurrogateAuthentication."))
                    ? [
                        {
                            value: "org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy",
                            text: "SURROGATE"
                        },
                        ...(
                            PalantirDashboardConfiguration.scriptFactoryAvailable()
                                ? [{
                                    value: "org.apereo.cas.services.GroovySurrogateRegisteredServiceAccessStrategy",
                                    text: "GROOVY SURROGATE"
                                }]
                                : []
                        )
                    ]
                    : []
            ),
            ...(
                CAS_FEATURES.includes("SCIM")
                    ? [{
                        value: "org.apereo.cas.scim.v2.access.ScimRegisteredServiceAccessStrategy",
                        text: "SCIM"
                    }]
                    : []
            ),
            ...(
                CAS_FEATURES.includes("Authorization")
                    ? [{
                        value: "org.apereo.cas.heimdall.services.HeimdallRegisteredServiceAccessStrategy",
                        text: "HEIMDALL"
                    }]
                    : []
            )
        ].filter(option => PalantirDashboardConfiguration.accessStrategyAvailable(option.value)),
        helpText: "Specifies the access strategy for the registered service.",
        changeEventHandlers: "handleAccessStrategyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide HttpRequestRegisteredServiceAccessStrategy",
        labelTitle: "IP Address",
        name: "registeredServiceAccessStrategyIpAddress",
        paramName: "accessStrategy.ipAddress",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the IP address to be used for access strategy."
    });

    createInputField({
        cssClasses: "hide HttpRequestRegisteredServiceAccessStrategy",
        labelTitle: "User Agent",
        name: "registeredServiceAccessStrategyUserAgent",
        paramName: "accessStrategy.userAgent",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the user agent to be used for access strategy."
    });

    createMappedInputField({
        cssClasses: "hide HttpRequestRegisteredServiceAccessStrategy",
        header: "Headers",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyHttpRequestHeaderName",
        keyLabel: "Header Name",
        valueField: "registeredServiceAccessStrategyHttpRequestHeaderValue",
        valueLabel: "Header Value",
        containerField: "accessStrategy.headers"
    });

    createSelectField({
        cssClasses: "hide GrouperRegisteredServiceAccessStrategy",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        labelTitle: "Group Field:",
        paramName: "accessStrategy.groupField",
        options: [
            {value: "", text: "NAME", selected: true},
            {value: "EXTENSION", text: "EXTENSION"},
            {value: "DISPLAY_NAME", text: "DISPLAY_NAME"},
            {value: "DISPLAY_EXTENSION", text: "DISPLAY_EXTENSION"}
        ],
        helpText: "Specifies the group field to be used when evaluating group membership."
    });

    createMappedInputField({
        cssClasses: "hide GrouperRegisteredServiceAccessStrategy",
        header: "Configuration Properties",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyGrouperConfigPropName",
        keyLabel: "Property Name",
        valueField: "registeredServiceAccessStrategyGrouperConfigPropValue",
        valueLabel: "Property Value",
        containerField: "accessStrategy.configProperties"
    });

    createInputField({
        cssClasses: "hide RemoteEndpointServiceAccessStrategy",
        labelTitle: "URL",
        name: "registeredServiceAccessStrategyEndpointUrl",
        paramName: "accessStrategy.endpointUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the remote endpoint URL to determine access strategy."
    });

    createInputField({
        cssClasses: "hide RemoteEndpointServiceAccessStrategy",
        labelTitle: "Acceptable Response Codes",
        name: "registeredServiceAccessStrategyEndpointAcceptableResponseCodes",
        paramName: "accessStrategy.acceptableResponseCodes",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the acceptable response codes from the remote endpoint to determine access strategy, separated by comma."
    });

    createInputField({
        cssClasses: "hide RemoteEndpointServiceAccessStrategy",
        labelTitle: "HTTP Method",
        name: "registeredServiceAccessStrategyEndpointHttpMethod",
        paramName: "accessStrategy.method",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the HTTP method (i.e. <code>GET, POST, etc</code>) to use when contacting the remote endpoint."
    });

    createMappedInputField({
        cssClasses: "hide RemoteEndpointServiceAccessStrategy",
        header: "Headers",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyEndpointHeaderName",
        keyLabel: "Header Name",
        valueField: "registeredServiceAccessStrategyEndpointHeaderValue",
        valueLabel: "Header Value",
        containerField: "accessStrategy.headers"
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyRegisteredServiceAccessStrategy",
            labelTitle: "Groovy Script",
            name: "registeredServiceAccessStrategyGroovyScript",
            paramName: "accessStrategy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAccessStrategy",
            title: "Specifies the Groovy script location, inline or external, to determine access strategy."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Access Strategy Groovy Script"
        });

        const surrogateScriptInput = createInputField({
            cssClasses: "hide GroovySurrogateRegisteredServiceAccessStrategy",
            labelTitle: "Groovy Script",
            name: "registeredServiceAccessStrategyGroovySurrogateScript",
            paramName: "accessStrategy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAccessStrategy",
            title: "Specifies the Groovy script location, inline or external, that decides whether a surrogate session may access the application."
        });
        attachPalantirGroovyEditorButton({
            target: surrogateScriptInput,
            title: "Surrogate Access Strategy Groovy Script"
        });
    }

    createRegisteredServiceExternalAccessStrategies();

    createInputField({
        cssClasses: "DefaultRegisteredServiceAccessStrategy",
        labelTitle: "Unauthorized Redirect URL",
        name: "registeredServiceAccessStrategyUnauthorizedRedirectUrl",
        paramName: "accessStrategy.unauthorizedRedirectUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the URL to redirect to when access is unauthorized."
    });

    CasDiscoveryProfile.fetchIfNeeded()
        .always(async () => {
            createMappedInputField({
                cssClasses: "DefaultRegisteredServiceAccessStrategy GrouperRegisteredServiceAccessStrategy ScimRegisteredServiceAccessStrategy",
                header: "Required Attributes",
                containerId: "registeredServiceAccessStrategyUnauthorizedRedirectUrlFieldContainer",
                keyField: "registeredServiceAccessStrategyReqAttrName",
                keyLabel: "Attribute Name",
                valueField: "registeredServiceAccessStrategyReqAttrValue",
                valueLabel: "Attribute Value",
                containerField: "accessStrategy.requiredAttributes",
                multipleValues: true,
                multipleValuesType: "java.util.HashSet",
                inclusion: "after"
            });

            createMappedInputField({
                cssClasses: "DefaultRegisteredServiceAccessStrategy",
                header: "Rejected Attributes",
                containerId: "registeredServiceAccessStrategyUnauthorizedRedirectUrlFieldContainer",
                keyField: "registeredServiceAccessStrategyRejectedAttrName",
                keyLabel: "Attribute Name",
                valueField: "registeredServiceAccessStrategyRejectedAttrValue",
                valueLabel: "Attribute Value",
                containerField: "accessStrategy.rejectedAttributes",
                multipleValues: true,
                multipleValuesType: "java.util.HashSet",
                inclusion: "after"
            });
        });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "Relation",
        name: "registeredServiceAccessStrategyOpenFGARelation",
        paramName: "accessStrategy.relation",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the OpenFGA relation used to evaluate access permissions."
    });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "Object",
        name: "registeredServiceAccessStrategyOpenFGAObject",
        paramName: "accessStrategy.object",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the OpenFGA object against which the relation is evaluated."
    });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "API URL",
        name: "registeredServiceAccessStrategyOpenFGAApiUrl",
        paramName: "accessStrategy.apiUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the OpenFGA API endpoint URL. Supports expression language."
    });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "Store ID",
        name: "registeredServiceAccessStrategyOpenFGAStoreId",
        paramName: "accessStrategy.storeId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the OpenFGA store identifier. Supports expression language."
    });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "Token",
        name: "registeredServiceAccessStrategyOpenFGAToken",
        paramName: "accessStrategy.token",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the authentication token used to access the OpenFGA API. Supports expression language."
    });

    createInputField({
        cssClasses: "hide OpenFGARegisteredServiceAccessStrategy",
        labelTitle: "User Type",
        name: "registeredServiceAccessStrategyOpenFGAUserType",
        paramName: "accessStrategy.userType",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the OpenFGA user type to be used when constructing access checks."
    });

    createInputField({
        cssClasses: "hide OpenPolicyAgentRegisteredServiceAccessStrategy",
        labelTitle: "Decision",
        name: "registeredServiceAccessStrategyOPADecision",
        paramName: "accessStrategy.decision",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Open Policy Agent decision or policy path used to evaluate access."
    });

    createInputField({
        cssClasses: "hide OpenPolicyAgentRegisteredServiceAccessStrategy",
        labelTitle: "API URL",
        name: "registeredServiceAccessStrategyOPAApiUrl",
        paramName: "accessStrategy.apiUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Open Policy Agent API endpoint URL. Supports expression language."
    });

    createInputField({
        cssClasses: "hide OpenPolicyAgentRegisteredServiceAccessStrategy",
        labelTitle: "Token",
        name: "registeredServiceAccessStrategyOPAToken",
        paramName: "accessStrategy.token",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the authentication token used to access the Open Policy Agent API. Supports expression language."
    });

    createMappedInputField({
        cssClasses: "hide OpenPolicyAgentRegisteredServiceAccessStrategy",
        header: "Context",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyOPAContextAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAccessStrategyOPAContextAttrValue",
        valueLabel: "Attribute Value",
        containerField: "accessStrategy.context"
    });


    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "API URL",
        name: "registeredServiceAccessStrategyPermifyApiUrl",
        paramName: "accessStrategy.apiUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permify API endpoint URL. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Tenant ID",
        name: "registeredServiceAccessStrategyPermifyTenantId",
        paramName: "accessStrategy.tenantId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permify tenant identifier. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Entity Type",
        name: "registeredServiceAccessStrategyPermifyEntityType",
        paramName: "accessStrategy.entityType",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permify entity type used in permission evaluation. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Subject Type",
        name: "registeredServiceAccessStrategyPermifySubjectType",
        paramName: "accessStrategy.subjectType",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the Permify subject type used in permission evaluation. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Subject Relation",
        name: "registeredServiceAccessStrategyPermifySubjectRelation",
        paramName: "accessStrategy.subjectRelation",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the relation between the subject and the entity in Permify. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Permission",
        name: "registeredServiceAccessStrategyPermifyPermission",
        paramName: "accessStrategy.permission",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the permission to be evaluated by Permify. Supports expression language."
    });

    createInputField({
        cssClasses: "hide PermifyRegisteredServiceAccessStrategy",
        labelTitle: "Token",
        name: "registeredServiceAccessStrategyPermifyToken",
        paramName: "accessStrategy.token",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the authentication token used to access the Permify API. Supports expression language."
    });

    createInputField({
        cssClasses: "hide TimeBasedRegisteredServiceAccessStrategy jquery-datepicker",
        data: {dateFormat: "yy-mm-dd'T'00:00:00"},
        labelTitle: "Starting Date/Time",
        name: "registeredServiceAccessStrategyTimeBasedStartingDateTime",
        paramName: "accessStrategy.startingDateTime",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the starting date and time from which access is allowed; picking a date starts at midnight. Supports expression language."
    });

    createInputField({
        cssClasses: "hide TimeBasedRegisteredServiceAccessStrategy jquery-datepicker",
        data: {dateFormat: "yy-mm-dd'T'23:59:59"},
        labelTitle: "Ending Date/Time",
        name: "registeredServiceAccessStrategyTimeBasedEndingDateTime",
        paramName: "accessStrategy.endingDateTime",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the ending date and time after which access is denied; picking a date ends at the last second of that day. Supports expression language."
    });

    createInputField({
        cssClasses: "hide TimeBasedRegisteredServiceAccessStrategy",
        labelTitle: "Zone ID",
        name: "registeredServiceAccessStrategyTimeBasedZoneId",
        paramName: "accessStrategy.zoneId",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the time zone identifier used to interpret the starting and ending date/time values. Supports expression language and defaults to UTC."
    });

}

function handleAuthenticationPolicyChange(select) {
    const type = getLastWord($(select).val());
    $(`#editServiceWizardMenuItemAuthenticationPolicy .${type}`).show();
    $("#editServiceWizardMenuItemAuthenticationPolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAuthenticationPolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAuthenticationPolicy [id$='FieldContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemAuthenticationPolicy");
    }
}

function createRegisteredServiceAuthenticationPolicy() {
    createSelectField({
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        labelTitle: "Type:",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy",
        paramName: "authenticationPolicy.criteria",
        options: [
            {
                value: "org.apereo.cas.services.AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
                text: "ALLOWED",
                selected: true,
                data: {
                    condition: function (value, $input) {
                        return $("#editServiceWizardMenuItemAuthenticationPolicy #registeredServiceAuthenticationPolicyRequiredHandlers").val().length > 0;
                    }
                }
            },
            {
                value: "org.apereo.cas.services.ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
                text: "EXCLUDED",
                data: {
                    condition: function (value, $input) {
                        return $("#editServiceWizardMenuItemAuthenticationPolicy #registeredServiceAuthenticationPolicyExcludedHandlers").val().length > 0;
                    }
                }
            },
            {
                value: "org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria",
                text: "ANY"
            },
            {
                value: "org.apereo.cas.services.AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
                text: "ALL"
            },
            {
                value: "org.apereo.cas.services.NotPreventedRegisteredServiceAuthenticationPolicyCriteria",
                text: "NOT PREVENTED"
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceAuthenticationPolicyCriteria",
                        text: "GROOVY"
                    }]
                    : []
            ),
            {
                value: "org.apereo.cas.services.RestfulRegisteredServiceAuthenticationPolicyCriteria",
                text: "REST"
            }
        ],
        helpText: "Specifies the authentication strategy for the registered service.",
        changeEventHandlers: "handleAuthenticationPolicyChange"
    }).data("renderer", function (value, $input) {
        const condition = $input.find(`option[value*='${value}']`).data("condition");
        let render = true;
        if (condition && typeof condition === "function") {
            render = condition(value, $input);
        }
        return render ? {"@class": value} : {};
    });

    createInputField({
        cssClasses: "hide RestfulRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "URL",
        name: "registeredServiceAuthenticationPolicyRestUrl",
        paramName: "authenticationPolicy.criteria.url",
        required: false,
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        title: "REST Endpoint URL"
    });

    createInputField({
        cssClasses: "hide RestfulRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "Basic Auth Username",
        name: "registeredServiceAuthenticationPolicyRestBasicAuthUsername",
        paramName: "authenticationPolicy.criteria.basicAuthUsername",
        required: false,
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        title: "Basic Auth Username"
    });

    createInputField({
        cssClasses: "hide RestfulRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "Basic Auth Password",
        name: "registeredServiceAuthenticationPolicyRestBasicAuthPassword",
        paramName: "authenticationPolicy.criteria.basicAuthPassword",
        required: false,
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        title: "Basic Auth Password"
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyRegisteredServiceAuthenticationPolicyCriteria",
            labelTitle: "Groovy Script",
            name: "registeredServiceAuthenticationPolicyGroovyScript",
            paramName: "authenticationPolicy.criteria.script",
            required: false,
            containerId: "editServiceWizardMenuItemAuthenticationPolicy",
            title: "Groovy Script"
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Authentication Policy Criteria Groovy Script"
        });
    }

    createInputField({
        cssClasses: "AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "Required Authentication Handler(s)",
        name: "registeredServiceAuthenticationPolicyRequiredHandlers",
        paramName: "authenticationPolicy.requiredAuthenticationHandlers",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy",
        required: false,
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        title: "Required Authentication Handler(s)"
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "hide ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "Excluded Authentication Handler(s)",
        name: "registeredServiceAuthenticationPolicyExcludedHandlers",
        paramName: "authenticationPolicy.excludedAuthenticationHandlers",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy",
        required: false,
        containerId: "editServiceWizardMenuItemAuthenticationPolicy",
        title: "Excluded Authentication Handler(s)"
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });
}
