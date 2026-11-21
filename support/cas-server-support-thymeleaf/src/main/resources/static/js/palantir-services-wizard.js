let editServiceWizardDialog = undefined;

/**
 * Deferred construction of the service wizard's fields.
 *
 * The wizard is a large form -- a couple of hundred fields across every protocol -- inside a dialog
 * that starts hidden and that most visits never open. Building it while the page parsed cost every
 * load that DOM, the MDC components attached to it, a dozen select widgets, and the discovery
 * profile request its attribute pickers make.
 *
 * The markup therefore registers each section's builder with palantirWizardField instead of calling it, and the
 * queue, palantirWizardFieldBuilders, is drained the first time the dialog is opened. Both are defined inline in the
 * dashboard page, since the markup registers its builders while the page is parsed, before the deferred scripts run.
 * Registration order is document order, which is what decides where each field lands, so the queue is drained in
 * order and awaited: one builder fetches the OpenID Connect discovery document before it can fill its dropdowns.
 */
let palantirWizardFieldsBuilt = null;

/**
 * Build the wizard in the background once the browser is idle, so that opening the first application does not
 * wait for it. Startup does not: this is asked for after the Applications page is set up.
 */
function prepareRegisteredServiceWizardWhenIdle() {
    const prepare = () => buildRegisteredServiceWizardFields();
    if ("requestIdleCallback" in window) {
        requestIdleCallback(prepare, {timeout: 5000});
    } else {
        setTimeout(prepare, 2000);
    }
}

/**
 * Build the wizard's fields, once.
 *
 * A builder that throws is logged and skipped: one section that cannot be built must not leave the
 * rest of the wizard empty. The fields are built after the Applications panel has had its widgets
 * set up, so the form's widgets are initialized here once the builders are done: a dropdown that the
 * wizard later turns into a bare selectmenu would carry no change handler, and picking a type in it
 * would not reveal that type's fields.
 *
 * @returns {Promise<void>} resolved when every registered builder has run
 */
function buildRegisteredServiceWizardFields() {
    if (!palantirWizardFieldsBuilt) {
        palantirWizardFieldsBuilt = (async () => {
            await new Promise(resolve => CasDiscoveryProfile.fetchIfNeeded().always(resolve));
            for (const builder of palantirWizardFieldBuilders) {
                try {
                    await builder();
                } catch (e) {
                    console.error("Unable to build a service wizard section:", e);
                }
            }
            initializePalantirWidgets(document.getElementById("editServiceWizardForm"));
            PalantirWizardSuggestions.attach(document.getElementById("editServiceWizardForm"));
            for (let attempt = 0; attempt < 20 && !$("#registeredServiceDescriptionEditor").data("trumbowyg"); attempt++) {
                await new Promise(resolve => setTimeout(resolve, 50));
            }
            performance.mark("palantir-wizard-built");
        })();
    }
    return palantirWizardFieldsBuilt;
}

function validatePattern(button, targetFieldId = "#registeredServiceId") {
    $("#validatePatternDialog").dialog({
        title: "Validate Pattern",
        modal: true,
        width: 600,
        closeOnEscape: false,
        buttons: {
            "Use Pattern": function () {
                $(targetFieldId).val($("#providedPattern").val()).focus();
                $(this).dialog("close");
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
        open: function () {
            $("#providedPattern").val($(targetFieldId).val());
        },
        close: function () {
            $(this).find("input").val("");
            $(this).dialog("destroy");
        }
    });
}

/**
 * Show the fields of the chosen attribute release policy and hide the others. The activation criteria, principal
 * attribute repository, value filter and consent sections nested in the attribute release section have types of their
 * own, so their fields are left to their own handlers.
 *
 * @param select the policy type select
 */
function handleAttributeReleasePolicyChange(select) {
    const type = getLastWord($(select).val());
    const section = "editServiceWizardMenuItemAttributeReleasePolicy";
    const own = selector => $(`#${section} ${selector}`)
        .filter((index, element) => $(element).parent().closest("[id^='editServiceWizardMenuItemAttributeRelease']").attr("id") === section);
    showElements(own(`.${type}`));
    hideElements(own("[id$='FieldContainer']").not(`.${type}`).not(".always-show"));
    hideElements(own("[id$='SelectContainer']").not(`.${type}`).not(".always-show"));
    hideElements(own("[id$='MapContainer']").not(`.${type}`));
    hideElements(own("[id$='ButtonPanel']").not(`.${type}`).not(".always-show"));

    if (!isPopulatingWizard) {
        own("[id$='FieldContainer']").find("input").val("");
        own("[id$='MapContainer']").find("input").val("");
        resetWizardSwitches(own("input[type='hidden'][data-switch-btn]").parent());
        own(".jqueryui-multiselectmenu").each(function () {
            this.tomselect?.clear();
        });
    }
}

const OIDC_SCOPE_ATTRIBUTE_RELEASE_POLICIES = [
    "OidcOpenIdScopeAttributeReleasePolicy",
    "OidcProfileScopeAttributeReleasePolicy",
    "OidcEmailScopeAttributeReleasePolicy",
    "OidcAddressScopeAttributeReleasePolicy",
    "OidcPhoneScopeAttributeReleasePolicy",
    "OidcOfflineAccessScopeAttributeReleasePolicy",
    "OidcAssuranceScopeAttributeReleasePolicy",
    "OidcCustomScopeAttributeReleasePolicy",
    "OidcScopeFreeAttributeReleasePolicy"
];

function createRegisteredServiceAttributeReleasePolicy() {
    createSelectField({
        cssClasses: "always-show",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        labelTitle: "Type:",
        paramName: "attributeReleasePolicy",
        options: [
            {
                value: "org.apereo.cas.services.DenyAllAttributeReleasePolicy",
                text: "DENY ALL",
                selected: true
            },
            {
                value: "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
                text: "RETURN ALL",
                data: {
                    markerClass: true
                }
            },
            {
                value: "org.apereo.cas.services.ReturnStaticAttributeReleasePolicy",
                text: "RETURN STATIC"
            },
            {
                value: "org.apereo.cas.services.ReturnEnvironmentAttributeReleasePolicy",
                text: "RETURN ENVIRONMENT"
            },
            {
                value: "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
                text: "RETURN ALLOWED"
            },
            {
                value: "org.apereo.cas.services.ReturnEncryptedAttributeReleasePolicy",
                text: "RETURN ENCRYPTED"
            },
            {
                value: "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
                text: "RETURN MAPPED"
            },
            {
                value: "org.apereo.cas.services.ReturnLinkedAttributeReleasePolicy",
                text: "RETURN LINKED"
            },
            {
                value: "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy",
                text: "PATTERN MATCHING"
            },
            {
                value: "org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy",
                text: "REST"
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyScriptAttributeReleasePolicy",
                        text: "GROOVY SCRIPT"
                    }]
                    : []
            )
        ],
        helpText: "Specifies the type of attribute release policy to apply.",
        changeEventHandlers: "handleAttributeReleasePolicyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        labelTitle: "Canonicalization Mode:",
        paramName: "attributeReleasePolicy.canonicalizationMode",
        options: [
            {
                value: "",
                text: "NONE",
                selected: true
            },
            {
                value: "UPPER",
                text: "UPPER"
            },
            {
                value: "LOWER",
                text: "LOWER"
            }
        ],
        helpText: "Specifies the canonicalization mode for attributes released."
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
                containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
                labelTitle: "Principal ID Attribute:",
                paramName: "attributeReleasePolicy.principalIdAttribute",
                title: "Define the principal ID to include in attribute release.",
                options: options,
                allowCreateOption: true
            });

            createMultiSelectField({
                cssClasses: "hide ReturnAllAttributeReleasePolicy",
                containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
                labelTitle: "Excluded Attributes:",
                paramName: "attributeReleasePolicy.excludedAttributes",
                title: "Define the attributes to be excluded from release, separated by comma.",
                options: options,
                allowCreateOption: true
            }).data("renderer", function (value) {
                return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
            });

            createMultiSelectField({
                id: "registeredServiceAttrReleaseAllowedAttrs",
                cssClasses: `hide ReturnAllowedAttributeReleasePolicy AttributeQueryAttributeReleasePolicy ${OIDC_SCOPE_ATTRIBUTE_RELEASE_POLICIES
                    .filter(policy => policy !== "OidcOpenIdScopeAttributeReleasePolicy").join(" ")}`,
                containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
                labelTitle: "Allowed Attributes:",
                paramName: "attributeReleasePolicy.allowedAttributes",
                title: "Define the attribute names for release, separated by comma. For an OpenID Connect standard scope, leave it empty to release the claims of that scope.",
                options: options,
                allowCreateOption: true
            }).data("renderer", function (value) {
                return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
            });

            createMultiSelectField({
                id: "registeredServiceAttrReleaseStaticAllowedAttrs",
                cssClasses: "hide ReturnStaticAttributeReleasePolicy",
                labelTitle: "Static Attributes:",
                paramName: "attributeReleasePolicy.allowedAttributes",
                containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
                title: "Define the attributes with static values for release, separated by comma.",
                options: options,
                allowCreateOption: true
            })
                .data("renderer", function (value) {
                    return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
                });

            cas.init("#editServiceWizardMenuItemAttributeReleasePolicy");
        });


    createInputField({
        cssClasses: "hide ReturnEncryptedAttributeReleasePolicy",
        labelTitle: "Encrypted Attributes (separated by comma)",
        name: "registeredServiceAttrReleaseEncryptedAttrs",
        paramName: "attributeReleasePolicy.allowedAttributes",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attributes to be encrypted for release, separated by comma."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });


    createInputField({
        cssClasses: "hide ReturnRestfulAttributeReleasePolicy",
        labelTitle: "Endpoint",
        name: "registeredServiceAttrReleaseRestEndpoint",
        paramName: "attributeReleasePolicy.endpoint",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the REST endpoint to collect attributes for release."
    });

    createInputField({
        cssClasses: "hide ReturnRestfulAttributeReleasePolicy",
        labelTitle: "Method (i.e. <code>GET, POST, etc</code>)",
        name: "registeredServiceAttrReleaseRestEndpointMethod",
        paramName: "attributeReleasePolicy.method",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the REST endpoint method to collect attributes for release."
    });

    createMappedInputField({
        cssClasses: "hide ReturnRestfulAttributeReleasePolicy",
        header: "Headers",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleaseRestHeaderName",
        keyLabel: "Header Name",
        valueField: "registeredServiceAttrReleasePolicyRestHeaderValue",
        valueLabel: "Header Value",
        containerField: "attributeReleasePolicy.headers"
    });

    createMappedInputField({
        cssClasses: "hide ReturnRestfulAttributeReleasePolicy",
        header: "Allowed Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyRestAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAttrReleasePolicyRestAttrValue",
        valueLabel: "Source Attributes",
        containerField: "attributeReleasePolicy.allowedAttributes",
        groovyEditorTitle: "REST Attribute Release Mapping Groovy Script"
    });

    createMappedInputField({
        cssClasses: "hide PatternMatchingAttributeReleasePolicy",
        header: "Allowed Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePatternMatchingAllowedAttrs",
        keyLabel: "Attribute",
        valueField: "registeredServiceAttrReleasePolicyPatternMatchingAttrValue",
        valueLabel: "Value Pattern",
        additionalFields: [
            {
                name: "transform",
                field: "registeredServiceAttrReleasePolicyPatternMatchingAttrTransform",
                label: "Transform",
                cssClasses: "ml-2",
                groovyEditorTitle: "Pattern-Matching Attribute Transform Groovy Script"
            }
        ],
        containerField: "attributeReleasePolicy.allowedAttributes",
        valueFieldRenderer: function ($inputKey, $inputValue, additionalInputs) {
            const rule = {
                "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy$Rule",
                "pattern": $inputValue.val().trim()
            };
            const transform = additionalInputs.transform?.val()?.trim();
            if (transform && transform.length > 0) {
                rule.transform = transform;
            }
            return rule;
        }
    });


    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyScriptAttributeReleasePolicy",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleaseGroovyScript",
            paramName: "attributeReleasePolicy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
            title: "Define the Groovy script location, inline or external, to determine attribute release."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Attribute Release Policy Groovy Script"
        });
    }

    createMappedInputField({
        cssClasses: "hide ReturnMappedAttributeReleasePolicy",
        header: "Allowed Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyMappedAttrName",
        keyLabel: "Source Attribute",
        valueField: "registeredServiceAttrReleasePolicyMappedAttrValue",
        valueLabel: "CAS Attribute",
        containerField: "attributeReleasePolicy.allowedAttributes",
        multipleValues: true,
        unwrapSingleElement: true,
        groovyEditorTitle: "Mapped Attribute Release Groovy Script"
    });

    createMappedInputField({
        cssClasses: "hide ReturnLinkedAttributeReleasePolicy",
        header: "Allowed Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyLinkedAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAttrReleasePolicyLinkedAttrValue",
        valueLabel: "Source Attributes (separated by comma)",
        containerField: "attributeReleasePolicy.allowedAttributes",
        multipleValues: true
    });

    createInputField({
        cssClasses: "hide OidcCustomScopeAttributeReleasePolicy",
        labelTitle: "Scope Name",
        name: "registeredServiceAttrReleaseOidcScopeName",
        paramName: "attributeReleasePolicy.scopeName",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the name of the custom scope whose claims this policy releases."
    });

    createMappedInputField({
        cssClasses: `hide ${OIDC_SCOPE_ATTRIBUTE_RELEASE_POLICIES.join(" ")}`,
        header: "Claim Mappings",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyOidcClaimName",
        keyLabel: "Claim",
        valueField: "registeredServiceAttrReleasePolicyOidcClaimAttribute",
        valueLabel: "Attribute",
        containerField: "attributeReleasePolicy.claimMappings"
    });

    createMappedInputField({
        cssClasses: "hide ReturnEnvironmentAttributeReleasePolicy",
        header: "Environment Variables",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyEnvVarAttrName",
        keyLabel: "Environment Variable Name",
        valueField: "registeredServiceAttrReleasePolicyEnvVarAttrValue",
        valueLabel: "Attribute Name",
        containerField: "attributeReleasePolicy.environmentVariables"
    });

    createMappedInputField({
        cssClasses: "hide ReturnEnvironmentAttributeReleasePolicy",
        header: "System Property",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicySysPropAttrName",
        keyLabel: "System Property",
        valueField: "registeredServiceAttrReleasePolicySysPropAttrValue",
        valueLabel: "Attribute Name",
        containerField: "attributeReleasePolicy.systemProperties"
    });


    createInputField({
        cssClasses: "hide AuthnRequestRequesterIdAttributeReleasePolicy",
        labelTitle: "Requester ID Pattern",
        name: "registeredServiceAttrReleasePolicyRequesterIdPatternAuthnRequest",
        paramName: "attributeReleasePolicy.requesterIdPattern",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the requester ID pattern to match for this policy."
    });

    createInputField({
        cssClasses: "hide AuthnRequestRequesterIdAttributeReleasePolicy",
        labelTitle: "Allowed Attributes (separated by comma)",
        name: "registeredServiceAttrReleasePolicyAllowedAttributesAuthnRequest",
        paramName: "attributeReleasePolicy.allowedAttributes",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attributes allowed to be released by this policy as a comma-separated list."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });


    createInputField({
        cssClasses: "hide MetadataEntityAttributesAttributeReleasePolicy",
        labelTitle: "Allowed Attributes (separated by comma)",
        name: "registeredServiceAttrReleasePolicyAllowedAttributesMetadataEntity",
        paramName: "attributeReleasePolicy.allowedAttributes",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attributes allowed to be released by this policy as a comma-separated list."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "hide MetadataEntityAttributesAttributeReleasePolicy",
        labelTitle: "Entity Attribute Values (separated by comma)",
        name: "registeredServiceAttrReleasePolicyEntityAttributeValues",
        paramName: "attributeReleasePolicy.entityAttributeValues",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the entity attribute values allowed to be released by this policy as a comma-separated list."
    })
        .data("renderer", function (value) {
            return ["java.util.LinkedHashSet", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "hide MetadataEntityAttributesAttributeReleasePolicy",
        labelTitle: "Entity Attribute",
        name: "registeredServiceAttrReleasePolicyMetadataEntityAttribute",
        paramName: "attributeReleasePolicy.entityAttribute",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the entity attribute to match for metadata-based attribute release."
    });
    createInputField({
        cssClasses: "hide MetadataEntityAttributesAttributeReleasePolicy",
        labelTitle: "Entity Attribute Format",
        name: "registeredServiceAttrReleasePolicyMetadataEntityAttributeFormat",
        paramName: "attributeReleasePolicy.entityAttributeFormat",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the entity attribute format to match for metadata-based attribute release."
    });

    createInputField({
        cssClasses: "hide MetadataEntityAttributesAttributeReleasePolicy InCommonRSAttributeReleasePolicy RefedsRSAttributeReleasePolicy AnonymousAccessAttributeReleasePolicy PseudonymousAccessAttributeReleasePolicy PersonalizedAccessAttributeReleasePolicy",
        labelTitle: "Entity ID Patterns (separated by comma)",
        name: "registeredServiceAttrReleasePolicyMetadataEntityIds",
        paramName: "attributeReleasePolicy.entityIds",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define regular expressions matched against the service provider entity ID; attributes are also released when its metadata does not carry the entity attribute but its entity ID matches one of them."
    })
        .data("renderer", function (value) {
            return ["java.util.LinkedHashSet", String(value).split(",").map(v => v.trim()).filter(v => v.length > 0)];
        });


    createInputField({
        cssClasses: "hide MetadataRegistrationAuthorityAttributeReleasePolicy",
        labelTitle: "Registration Authority",
        name: "registeredServiceAttrReleasePolicyMetadataRegistrationAuthority",
        paramName: "attributeReleasePolicy.registrationAuthority",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the registration authority pattern for metadata-based attribute release."
    });

    createInputField({
        cssClasses: "hide MetadataRegistrationAuthorityAttributeReleasePolicy",
        labelTitle: "Allowed Attributes (separated by comma)",
        name: "registeredServiceAttrReleasePolicyAllowedAttributesMetadataRegistrationAuthority",
        paramName: "attributeReleasePolicy.allowedAttributes",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attributes allowed to be released by this policy as a comma-separated list."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "hide PatternMatchingEntityIdAttributeReleasePolicy",
        labelTitle: "Allowed Attributes (separated by comma)",
        name: "registeredServiceAttrReleasePolicyAllowedAttributesPatternMatchingEntityId",
        paramName: "attributeReleasePolicy.allowedAttributes",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attributes allowed to be released by this policy as a comma-separated list."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "hide PatternMatchingEntityIdAttributeReleasePolicy",
        labelTitle: "Entity IDs",
        name: "registeredServiceAttrReleasePolicyEntityIdsPatternMatching",
        paramName: "attributeReleasePolicy.entityIds",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the entity IDs patterns to match for this policy"
    });

    const groovySamlScriptInput = createInputField({
        cssClasses: "hide GroovySamlRegisteredServiceAttributeReleasePolicy",
        labelTitle: "Groovy Script",
        name: "registeredServiceAttrReleasePolicyGroovySamlScript",
        paramName: "attributeReleasePolicy.groovyScript",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the Groovy script location to determine attribute release."
    });
    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        attachPalantirGroovyEditorButton({
            target: groovySamlScriptInput,
            title: "SAML Attribute Release Policy Groovy Script"
        });
    }


    createInputField({
        cssClasses: "hide EduPersonTargetedIdAttributeReleasePolicy",
        labelTitle: "Salt",
        name: "registeredServiceAttrReleasePolicyEduPersonTargetedIdSalt",
        paramName: "attributeReleasePolicy.salt",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the salt value used in generating the EduPersonTargetedID."
    });
    createInputField({
        cssClasses: "hide EduPersonTargetedIdAttributeReleasePolicy",
        labelTitle: "Attribute",
        name: "registeredServiceAttrReleasePolicyEduPersonTargetedIdAttribute",
        paramName: "attributeReleasePolicy.attribute",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the attribute name to store the EduPersonTargetedID."
    });


}

function createRegisteredServiceAttributeReleaseConsentPolicy() {
    createSelectField({
        cssClasses: "always-show",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        labelTitle: "Consent Policy:",
        paramName: "attributeReleasePolicy.consentPolicy",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
                text: "DEFAULT"
            }
        ],
        helpText: "Specifies the consent policy for attribute release.",
        changeEventHandlers: "handleAttributeReleaseConsentPolicyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        paramType: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        cssClasses: "hide DefaultRegisteredServiceConsentPolicy",
        labelTitle: "Include Only Attributes",
        name: "registeredServiceAttrReleaseConsentIncludedAttrs",
        paramName: "attributeReleasePolicy.consentPolicy.includeOnlyAttributes",
        required: false,
        title: "Define the attribute names for release, separated by comma."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });

    createSelectField({
        cssClasses: "hide DefaultRegisteredServiceConsentPolicy",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        labelTitle: "Status:",
        paramName: "attributeReleasePolicy.consentPolicy.status",
        options: [
            {value: "UNDEFINED", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specify whether consent is enabled for attribute release."
    });

    createInputField({
        paramType: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        cssClasses: "hide DefaultRegisteredServiceConsentPolicy",
        labelTitle: "Excluded Attributes (separated by comma)",
        name: "registeredServiceAttrReleaseConsentExcludedAttrs",
        paramName: "attributeReleasePolicy.consentPolicy.excludedAttributes",
        required: false,
        title: "Attributes released without asking for consent, separated by comma."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").map(entry => entry.trim()).filter(entry => entry.length > 0)];
        });

    createInputField({
        paramType: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        cssClasses: "hide DefaultRegisteredServiceConsentPolicy",
        labelTitle: "Excluded Services (separated by comma)",
        name: "registeredServiceAttrReleaseConsentExcludedServices",
        paramName: "attributeReleasePolicy.consentPolicy.excludedServices",
        required: false,
        title: "Patterns of service URLs for which consent is not asked, separated by comma."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").map(entry => entry.trim()).filter(entry => entry.length > 0)];
        });

    createInputField({
        paramType: "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueConsentPolicy",
        cssClasses: "hide DefaultRegisteredServiceConsentPolicy",
        labelTitle: "Order",
        name: "registeredServiceAttrReleaseConsentOrder",
        paramName: "attributeReleasePolicy.consentPolicy.order",
        required: false,
        dataType: "number",
        title: "The position of this consent policy when consent policies are combined; lower runs first."
    });
}

function handleAttributeReleaseConsentPolicyChange(select) {
    let type = getLastWord($(select).val());
    if (type.length === 0) {
        type = "UNDEFINED";
    }
    $(`#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy .${type}`).show();
    $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='SelectContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='SelectContainer'] input")
            .val("");
    }
}

function handleAttributeReleasePolicyActivationCriteriaChange(select) {
    let type = getLastWord($(select).val());
    if (type.length === 0) {
        type = "UNDEFINED";
    }
    $(`#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria .${type}`).show();
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='ButtonPanel']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();

    // Only clear values when not populating from an existing service
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='MapContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria");
    }
}

function createRegisteredServiceAttributeReleasePolicyActivationCriteria() {
    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
        labelTitle: "Activation Criteria:",
        changeEventHandlers: "handleAttributeReleasePolicyActivationCriteriaChange",
        paramName: "attributeReleasePolicy.activationCriteria",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
                text: "ATTRIBUTES"
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
                        text: "GROOVY"
                    }]
                    : []
            )

        ],
        helpText: "Specifies the activation criteria for the attribute release policy."
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
        labelTitle: "Operator (i.e. <code>AND, OR</code>)",
        name: "registeredServiceAttrReleaseActivationCriteriaAttributesOperator",
        paramName: "attributeReleasePolicy.activationCriteria.operator",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
        title: "Define the operator to use when evaluating attributes for activation criteria."
    });

    createMappedInputField({
        cssClasses: "hide AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
        header: "Required Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
        keyField: "registeredServiceAttrReleasePolicyAttributeActivationCriteriaAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAttrReleasePolicyAttributeActivationCriteriaAttrValue",
        valueLabel: "Attribute Values (separated by comma)",
        containerField: "attributeReleasePolicy.activationCriteria.requiredAttributes",
        multipleValues: true
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            paramType: "org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
            cssClasses: "hide GroovyRegisteredServiceAttributeReleaseActivationCriteria",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleasePolicyAttributeActivationCriteriaGroovyScript",
            paramName: "attributeReleasePolicy.activationCriteria.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
            title: "Specifies the Groovy script location, inline or external, to determine attribute release activation criteria."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Attribute Release Activation Criteria Groovy Script"
        });
    }
}

function handleAttributeReleasePolicyPrincipalAttributeRepositoryChange(select) {
    let type = getLastWord($(select).val());
    if (type.length === 0) {
        type = "UNDEFINED";
    }
    $(`#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository .${type}`).show();
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='ButtonPanel']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();

    // Only clear values when not populating from an existing service
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='MapContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository");
    }
}

function createRegisteredServiceAttributeReleasePrincipalAttributesRepository() {
    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository",
        labelTitle: "Principal Attribute Repository:",
        changeEventHandlers: "handleAttributeReleasePolicyPrincipalAttributeRepositoryChange",
        paramName: "attributeReleasePolicy.principalAttributesRepository",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository",
                text: "DEFAULT"
            },
            {
                value: "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
                text: "CACHED"
            }

        ],
        helpText: "Specifies the principal attributes repository for the attribute release policy."
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide DefaultPrincipalAttributesRepository CachingPrincipalAttributesRepository",
        labelTitle: "Attribute Repository IDs",
        name: "registeredServiceAttrReleasePolicyPrincipalAttributeRepositoryIds",
        paramName: "attributeReleasePolicy.principalAttributesRepository.attributeRepositoryIds",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository",
        title: "Define the attribute repository IDs to be used, separated by comma."
    }).data("renderer", function (value) {
        return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
    });

    createSelectField({
        cssClasses: "hide DefaultPrincipalAttributesRepository CachingPrincipalAttributesRepository",
        containerId: "editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository",
        labelTitle: "Merging Strategy:",
        paramName: "attributeReleasePolicy.principalAttributesRepository.mergingStrategy",
        options: [

            {
                value: "",
                text: "MULTIVALUED",
                selected: true
            },
            {
                value: "REPLACE",
                text: "REPLACE"
            },
            {
                value: "ADD",
                text: "ADD"
            },
            {
                value: "SOURCE",
                text: "SOURCE"
            },
            {
                value: "DESTINATION",
                text: "DESTINATION"
            }
        ],
        helpText: "Specifies the merging strategy for principal attributes."
    });

    createInputField({
        cssClasses: "hide CachingPrincipalAttributesRepository",
        labelTitle: "Time Unit (e.g., <code>SECONDS, MINUTES, HOURS, DAYS</code>)",
        name: "registeredServiceAttrReleasePolicyPrincipalAttributeCachingTimeUnit",
        paramName: "attributeReleasePolicy.principalAttributesRepository.timeUnit",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository",
        title: "Define the time unit for caching principal attributes (e.g., <code>SECONDS, MINUTES, HOURS, DAYS</code>)."
    });

    createInputField({
        cssClasses: "hide CachingPrincipalAttributesRepository",
        labelTitle: "Expiration",
        dataType: "number",
        name: "registeredServiceAttrReleasePolicyPrincipalAttributeCachingExpiration",
        paramName: "attributeReleasePolicy.principalAttributesRepository.expiration",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository",
        title: "Define the expiration value for cached principal attributes."
    });

}

function handleAttributeReleasePolicyValueFilterChange(select) {
    let type = getLastWord($(select).val());
    if (type.length === 0) {
        type = "UNDEFINED";
    }
    $(`#editServiceWizardMenuItemAttributeReleaseValueFilters .${type}`).show();
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='ButtonPanel']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();

    // Only clear values when not populating from an existing service
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='MapContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemAttributeReleaseValueFilters");
    }
}

function createRegisteredServiceAttributeReleaseValueFilters() {
    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
        labelTitle: "Value Filter:",
        changeEventHandlers: "handleAttributeReleasePolicyValueFilterChange",
        paramName: "attributeReleasePolicy.attributeFilter",
        options: [
            {
                value: "",
                text: "UNDEFINED",
                selected: true
            },
            {
                value: "org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter",
                text: "REGEX"
            },
            {
                value: "org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter",
                text: "MAPPED REGEX"
            },
            {
                value: "org.apereo.cas.services.support.RegisteredServiceReverseMappedRegexAttributeFilter",
                text: "REVERSE MAPPED REGEX"
            },
            {
                value: "org.apereo.cas.services.support.RegisteredServiceMutantRegexAttributeFilter",
                text: "MUTANT MAPPED REGEX"
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter",
                        text: "GROOVY"
                    }]
                    : []
            )
        ],
        helpText: "Specifies the attribute value filter for the attribute release policy."
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide RegisteredServiceRegexAttributeFilter",
        labelTitle: "Pattern",
        name: "registeredServiceAttrReleasePolicyAttributeValueFilterPattern",
        paramName: "attributeReleasePolicy.attributeFilter.pattern",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
        title: "Define the regex pattern to filter attribute values."
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide RegisteredServiceScriptedAttributeFilter",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleasePolicyAttributeValueFilterScript",
            paramName: "attributeReleasePolicy.attributeFilter.script",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
            title: "Specifies the Groovy script location, inline or external, to filter attribute values."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Attribute Value Filter Groovy Script"
        });
    }

    createMappedInputField({
        cssClasses: "hide RegisteredServiceMutantRegexAttributeFilter",
        header: "Patterns",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
        keyField: "registeredServiceAttrReleasePolicyAttributeValueFilterMutantAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAttrReleasePolicyAttributeValueFilterMutantAttrValue",
        valueLabel: "Attribute Values (separated by comma)",
        containerField: "attributeReleasePolicy.attributeFilter.patterns",
        multipleValues: true,
        unwrapSingleElement: true
    });

    createMappedInputField({
        cssClasses: "hide RegisteredServiceMappedRegexAttributeFilter RegisteredServiceReverseMappedRegexAttributeFilter",
        header: "Patterns",
        containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
        keyField: "registeredServiceAttrReleasePolicyAttributeValueFilterAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAttrReleasePolicyAttributeValueFilterAttrValue",
        valueLabel: "Attribute Value",
        containerField: "attributeReleasePolicy.attributeFilter.patterns"
    });

}

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
            const options = CasDiscoveryProfile.availableAttributes().map(attr => ({
                value: attr,
                text: attr
            }));
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
                autoComplete: options,
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
                autoComplete: options,
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

function createRegisteredServiceProperties() {
    createMappedInputField({
        containerId: "editServiceWizardMenuItemProperties",
        keyField: "registeredServicePropertyName",
        keyLabel: "Property Name",
        valueField: "registeredServicePropertyValue",
        valueLabel: "Property Value(s) Separated by Comma",
        containerField: "properties",
        autoComplete: PalantirDashboardConfiguration.serviceProperties(),
        valueFieldRenderer: function ($inputKey, $inputValue) {
            return {
                "@class": "org.apereo.cas.services.DefaultRegisteredServiceProperty",
                "values": ["java.util.HashSet", $inputValue.val().split(",")]
            };
        }
    });
}

function createRegisteredServiceContacts(index) {
    const details = ["name", "email", "phone", "department", "type"];
    details.forEach(detail => {
        createInputField({
            labelTitle: capitalize(detail),
            name: `registeredServiceContact${capitalize(detail)}${index}`,
            paramName: `contacts`,
            required: false,
            containerId: `registeredServiceContact${index}-tab`,
            title: `Specify the contact's ${detail}`
        })
            .data("renderer", function (value, $input, serviceDefinition) {
                return ["java.util.ArrayList", []];
            })
            .data("beforeGenerate", function ($input, serviceDefinition) {
                if ($input.val().length > 0) {
                    const slots = wizardContactSlots.get(serviceDefinition) ?? new Map();
                    wizardContactSlots.set(serviceDefinition, slots);
                    const contact = slots.get(index) ?? {"@class": "org.apereo.cas.services.DefaultRegisteredServiceContact"};
                    contact[detail] = $input.val();
                    slots.set(index, contact);
                }
            })
        ;
    });

}

function createRegisteredServicePublicKey() {
    createInputField({
        paramType: "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
        labelTitle: "Public Key Location",
        name: "registeredServicePublicKeyLocation",
        paramName: "publicKey.location",
        required: false,
        containerId: "editServiceWizardMenuItemPublicKey",
        title: "Specifies the location of the public key used for encrypting attributes or the username."
    });

    createInputField({
        paramType: "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
        labelTitle: "Algorithm",
        name: "registeredServicePublicKeyAlgorithm",
        paramName: "publicKey.algorithm",
        required: false,
        containerId: "editServiceWizardMenuItemPublicKey",
        title: "Specifies the algorithm used with the public key (i.e. <code>RSA</code>)."
    });
}

function createRegisteredServiceAcceptableUsagePolicy() {
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy",
        labelTitle: "Message Code",
        name: "registeredServiceAUPMessageCode",
        paramName: "acceptableUsagePolicy.messageCode",
        required: false,
        containerId: "editServiceWizardMenuItemAUP",
        title: "Specifies the message code to be shown to the user for the acceptable usage policy."
    });
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy",
        labelTitle: "Text",
        name: "registeredServiceAUPText",
        paramName: "acceptableUsagePolicy.text",
        required: false,
        containerId: "editServiceWizardMenuItemAUP",
        title: "Specifies the text to be shown to the user for the acceptable usage policy."
    });
}

function handleSsoParticipationPolicyChange(select) {
    let type = getLastWord($(select).val());
    if (type.length === 0) {
        type = "DefaultRegisteredServiceSingleSignOnParticipationPolicy";
    }
    $(`#editServiceWizardMenuItemSSOParticipationPolicy .${type}`).show();
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='SelectContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .hide();
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();

    // Only clear values when not populating from an existing service
    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='FieldContainer'] input")
            .val("");
        $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='MapContainer'] input")
            .val("");
        resetWizardSwitches("#editServiceWizardMenuItemSSOParticipationPolicy");
    }
}

function createRegisteredServiceSsoParticipationPolicy() {
    createSelectField({
        cssClasses: "always-show",
        containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
        labelTitle: "SSO Participation Policy:",
        paramName: "singleSignOnParticipationPolicy",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceSingleSignOnParticipationPolicy",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {
                value: "org.apereo.cas.services.AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy",
                text: "ATTRIBUTES"
            },
            {
                value: "org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy",
                text: "LAST USED"
            },
            {
                value: "org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy",
                text: "AUTHENTICATION DATE"
            },
            {
                value: "org.apereo.cas.services.NeverRegisteredServiceSingleSignOnParticipationPolicy",
                text: "NEVER",
                data: {
                    markerClass: true
                }
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceSingleSignOnParticipationPolicy",
                        text: "GROOVY"
                    }]
                    : []
            )
        ],
        helpText: "Specifies the single sign-on participation policy for the registered service.",
        changeEventHandlers: "handleSsoParticipationPolicyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy",
        labelTitle: "Time Unit (i.e. SECONDS, MINUTES, HOURS, DAYS)",
        name: "registeredServiceSSOParticipationTimeUnit",
        paramName: "singleSignOnParticipationPolicy.timeUnit",
        required: false,
        containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
        title: "Specifies the time unit for the SSO participation policy (i.e <code>SECONDS, MINUTES, HOURS, DAYS</code>)."
    });

    createInputField({
        cssClasses: "hide LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy",
        labelTitle: "Time Value",
        dataType: "number",
        name: "registeredServiceSSOParticipationTimeValue",
        paramName: "singleSignOnParticipationPolicy.timeValue",
        required: false,
        containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
        title: "Specifies the time value for the SSO participation policy."
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyRegisteredServiceSingleSignOnParticipationPolicy",
            labelTitle: "Groovy Script",
            name: "registeredServiceSSOParticipationGroovyValue",
            paramName: "singleSignOnParticipationPolicy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
            title: "Specifies the Groovy script location, inline or external, to be used to generate the username."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Single Sign-On Participation Policy Groovy Script"
        });
    }

    createSelectField({
        cssClasses: "DefaultRegisteredServiceSingleSignOnParticipationPolicy",
        containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
        labelTitle: "Create Cookie on Renewed Authentications:",
        paramName: "singleSignOnParticipationPolicy.createCookieOnRenewedAuthentication",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceSingleSignOnParticipationPolicy",
        options: [
            {value: "", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specifies whether to create the SSO cookie on renewed authentications for non-SSO-participating applications"
    });


    createMappedInputField({
        cssClasses: "hide AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy",
        header: "Attributes",
        containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
        keyField: "registeredServiceSSOParticipationAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceSSOParticipationAttrValue",
        valueLabel: "Attribute Value",
        containerField: "singleSignOnParticipationPolicy.attributes",
        multipleValues: true
    });
}

function createRegisteredServiceWebflowInterruptPolicy() {
    createSelectField({
        containerId: "editServiceWizardMenuItemWebflowInterruptPolicy",
        labelTitle: "Force Execution:",
        paramName: "webflowInterruptPolicy.forceExecution",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
        options: [
            {value: "", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specifies whether to force execution of the webflow interrupt policy."
    });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
        labelTitle: "Only for Users with Attribute (pattern)",
        name: "registeredServiceWebflowInterruptPolicyAttributeName",
        paramName: "webflowInterruptPolicy.attributeName",
        required: false,
        containerId: "editServiceWizardMenuItemWebflowInterruptPolicy",
        title: "Interrupt only users with an attribute whose name matches this pattern, and whose value matches the value pattern."
    });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
        labelTitle: "And Attribute Value (pattern)",
        name: "registeredServiceWebflowInterruptPolicyAttributeValue",
        paramName: "webflowInterruptPolicy.attributeValue",
        required: false,
        containerId: "editServiceWizardMenuItemWebflowInterruptPolicy",
        title: "Interrupt only users with a value of the attribute that matches this pattern; both the name and the value must be set."
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            paramType: "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
            labelTitle: "Groovy Script",
            name: "registeredServiceWebflowInterruptPolicyGroovyScript",
            paramName: "webflowInterruptPolicy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemWebflowInterruptPolicy",
            title: "A Groovy script, inline or external, given the attributes, username, application and service; the user is not interrupted when it returns false."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Webflow Interrupt Policy Groovy Script"
        });
    }
}

/**
 * Show the fields of the chosen type of a policy and hide those of the other types. When the operator chooses the
 * type, rather than a definition filling it in, the fields are also emptied and the switches put back, so the
 * settings of another type are not saved with it.
 *
 * @param containerSelector the section that holds the policy's fields
 * @param select the list that chooses the policy's type
 */
function showWizardPolicyType(containerSelector, select) {
    const container = $(containerSelector);
    const type = getLastWord($(select).val()) || "UNDEFINED";
    hideElements(container.find("[id$='FieldContainer'], [id$='SelectContainer'], [id$='SwitchButtonPanel'], [id$='MapContainer']")
        .not(".always-show")
        .not(`.${type}`));
    showElements(container.find(`.${type}`));
    if (!isPopulatingWizard) {
        container.find("[id$='FieldContainer'] input, [id$='MapContainer'] input").val("");
        resetWizardSwitches(container);
    }
}

function handleDelegatedAuthenticationPolicyChange(select) {
    showWizardPolicyType("#editServiceWizardMenuItemDelegatedAuthenticationPolicy", select);
}

/**
 * The delegated authentication policy of the access strategy: which external identity providers the application's
 * users may sign in with, whether any may be used when none is listed, whether the listed provider is used
 * exclusively, and a Groovy script that picks the provider.
 */
function createRegisteredServiceDelegatedAuthenticationPolicy() {
    const containerId = "editServiceWizardMenuItemDelegatedAuthenticationPolicy";
    const policy = "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy";
    createSelectField({
        cssClasses: "always-show",
        containerId: containerId,
        labelTitle: "Policy:",
        id: "registeredServiceDelegatedAuthenticationPolicyType",
        paramName: "accessStrategy.delegatedAuthenticationPolicy",
        options: [
            {value: "", text: "UNDEFINED", selected: true},
            {value: policy, text: "DEFAULT"}
        ],
        helpText: "Choose DEFAULT to limit the external identity providers this application may use.",
        changeEventHandlers: "handleDelegatedAuthenticationPolicyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide DefaultRegisteredServiceDelegatedAuthenticationPolicy",
        paramType: policy,
        labelTitle: "Allowed Identity Providers (separated by comma)",
        name: "registeredServiceDelegatedAuthenticationAllowedProviders",
        paramName: "accessStrategy.delegatedAuthenticationPolicy.allowedProviders",
        required: false,
        containerId: containerId,
        title: "The names of the external identity providers this application's users may sign in with."
    }).data("renderer", function (value) {
        return ["java.util.ArrayList", value.split(",").map(entry => entry.trim()).filter(entry => entry.length > 0)];
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide DefaultRegisteredServiceDelegatedAuthenticationPolicy",
            paramType: policy,
            labelTitle: "Provider Selection Groovy Script",
            name: "registeredServiceDelegatedAuthenticationSelectionStrategy",
            paramName: "accessStrategy.delegatedAuthenticationPolicy.selectionStrategy",
            required: false,
            containerId: containerId,
            title: "A Groovy script, inline or external, given the request, service, application and the available providers, that returns the provider to redirect to."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Delegated Authentication Provider Selection Groovy Script"
        });
    }
}

function handleAccessStrategyActivationCriteriaChange(select) {
    showWizardPolicyType("#editServiceWizardMenuItemAccessStrategyActivation", select);
}

/**
 * Whether the access strategy's activation can be chosen: only the default access strategy checks required and
 * rejected attributes, so only it has activation criteria. Choosing another type of access strategy puts the
 * activation back to undefined.
 *
 * @param type the short name of the chosen access strategy type
 */
function toggleAccessStrategyActivation(type) {
    const header = $("#registeredServiceAccessStrategyActivation");
    if (type === "DefaultRegisteredServiceAccessStrategy") {
        showElements(header);
    } else {
        hideElements(header);
        if (!isPopulatingWizard) {
            const select = $("#editServiceWizardMenuItemAccessStrategyActivation select[data-param-name='accessStrategy.activationCriteria']");
            select.val("");
            if (select.selectmenu("instance")) {
                select.selectmenu("refresh");
            }
            handleAccessStrategyActivationCriteriaChange(select);
        }
    }
    RegisteredServiceSections.refresh?.();
}

/**
 * The activation criteria of the default access strategy: when it checks its required and rejected attributes, by
 * the user's attributes or a Groovy script, and whether access is granted when it does not.
 */
function createRegisteredServiceAccessStrategyActivationCriteria() {
    const containerId = "editServiceWizardMenuItemAccessStrategyActivation";
    const byAttributes = "org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria";
    const byScript = "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyActivationCriteria";
    createSelectField({
        cssClasses: "always-show",
        containerId: containerId,
        labelTitle: "Activation:",
        id: "registeredServiceAccessStrategyActivationCriteria",
        paramName: "accessStrategy.activationCriteria",
        options: [
            {value: "", text: "ALWAYS", selected: true},
            {value: byAttributes, text: "ATTRIBUTES"},
            ...(PalantirDashboardConfiguration.scriptFactoryAvailable() ? [{value: byScript, text: "GROOVY"}] : [])
        ],
        helpText: "Specifies when the access strategy checks its required and rejected attributes.",
        changeEventHandlers: "handleAccessStrategyActivationCriteriaChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createSelectField({
        cssClasses: "hide AttributeBasedRegisteredServiceAccessStrategyActivationCriteria",
        containerId: containerId,
        labelTitle: "Operator:",
        id: "registeredServiceAccessStrategyActivationOperator",
        paramName: "accessStrategy.activationCriteria.operator",
        options: [
            {value: "AND", text: "AND: Every Attribute Must Match", selected: true},
            {value: "OR", text: "OR: Any Attribute May Match"}
        ],
        helpText: "Specifies how the attributes below are combined."
    });

    createMappedInputField({
        cssClasses: "hide AttributeBasedRegisteredServiceAccessStrategyActivationCriteria",
        header: "Activating Attributes",
        containerId: containerId,
        keyField: "registeredServiceAccessStrategyActivationAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAccessStrategyActivationAttrValue",
        valueLabel: "Attribute Values (separated by comma)",
        containerField: "accessStrategy.activationCriteria.requiredAttributes",
        multipleValues: true
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyRegisteredServiceAccessStrategyActivationCriteria",
            paramType: byScript,
            labelTitle: "Groovy Script",
            name: "registeredServiceAccessStrategyActivationGroovyScript",
            paramName: "accessStrategy.activationCriteria.groovyScript",
            required: false,
            containerId: containerId,
            title: "A Groovy script, inline or external, that returns true when the access strategy should check its attributes."
        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Access Strategy Activation Groovy Script"
        });
    }
}

function handleUsernameAttributeProviderChange(select) {
    const type = getLastWord($(select).val());
    showElements($(`#editServiceWizardMenuItemUsernameAttribute .${type}`));
    hideElements($("#editServiceWizardMenuItemUsernameAttribute [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .not(".DefaultRegisteredServiceUsernameProvider"));
    hideElements($("#editServiceWizardMenuItemUsernameAttribute [id$='SelectContainer']")
        .not(`.${type}`)
        .not(".always-show")
        .not(".DefaultRegisteredServiceUsernameProvider"));

    if (!isPopulatingWizard) {
        $("#editServiceWizardMenuItemUsernameAttribute [id$='FieldContainer'] input")
            .val("");
        wizardPersistentIdGeneratorType = null;
    }

    const hideDefaults = $(select).find(":selected").data("hideDefaults");
    if (hideDefaults) {
        hideElements($("#editServiceWizardMenuItemUsernameAttribute .DefaultRegisteredServiceUsernameProvider"));
    } else {
        showElements($("#editServiceWizardMenuItemUsernameAttribute .DefaultRegisteredServiceUsernameProvider"));
    }
}

/**
 * The type of the persistent identifier generator of the application the wizard opened, so that saving keeps it rather
 * than replacing it with the default type of the username provider. It is forgotten when the operator chooses another
 * username provider, and when the wizard opens.
 */
let wizardPersistentIdGeneratorType = null;

/**
 * One setting of the username provider's persistent identifier generator, with the generator's type: the type of the
 * opened application's generator, otherwise the pairwise generator for OpenID Connect pairwise identifiers and the
 * Shibboleth-compatible generator for anonymous ones.
 *
 * @param property the generator's setting
 * @param value the field's text
 * @returns {object} the generator, holding that setting
 */
function renderWizardPersistentIdGenerator(property, value) {
    const provider = getLastWord($("#registeredServiceUsernameAttributeProvider").val());
    const type = wizardPersistentIdGeneratorType ?? (provider === "PairwiseOidcRegisteredServiceUsernameAttributeProvider"
        ? "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator"
        : "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator");
    return {"@class": type, [property]: value.trim()};
}

/**
 * Read one setting of the opened application's persistent identifier generator, noting the generator's type.
 *
 * @param property the generator's setting
 * @param generator the generator, if the application has one
 * @returns {string|undefined} the setting's value
 */
function readWizardPersistentIdGenerator(property, generator) {
    wizardPersistentIdGeneratorType = generator?.["@class"] ?? null;
    return generator?.[property];
}

function createRegisteredServiceUsernameAttributeProvider() {
    createSelectField({
        cssClasses: "always-show",
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        labelTitle: "Type:",
        paramName: "usernameAttributeProvider",
        options: [
            {
                value: "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider",
                text: "DEFAULT",
                selected: true
            },
            {
                value: "org.apereo.cas.services.StaticRegisteredServiceUsernameProvider",
                text: "STATIC",
                data: {
                    hideDefaults: true
                }
            },
            {
                value: "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider",
                text: "ANONYMOUS",
                data: {
                    hideDefaults: true,
                    markerClass: true
                }
            },
            ...(
                PalantirDashboardConfiguration.scriptFactoryAvailable()
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider",
                        text: "GROOVY"
                    }]
                    : []
            ),

            {
                value: "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
                text: "PRINCIPAL ATTRIBUTE"
            }
        ],
        helpText: "Specifies the strategy to determine the username attribute.",
        changeEventHandlers: "handleUsernameAttributeProviderChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });

    createInputField({
        cssClasses: "hide StaticRegisteredServiceUsernameProvider",
        labelTitle: "Value",
        name: "registeredServiceUsernameAttributeStaticValue",
        paramName: "usernameAttributeProvider.value",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies the static value to be used as the username."
    });

    if (PalantirDashboardConfiguration.scriptFactoryAvailable()) {
        const scriptInput = createInputField({
            cssClasses: "hide GroovyRegisteredServiceUsernameProvider",
            labelTitle: "Groovy Script",
            name: "registeredServiceUsernameAttributeGroovyValue",
            paramName: "usernameAttributeProvider.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemUsernameAttribute",
            title: "Specifies the Groovy script location, inline or external, to be used to generate the username."

        });
        attachPalantirGroovyEditorButton({
            target: scriptInput,
            title: "Username Attribute Provider Groovy Script"
        });
    }

    createInputField({
        cssClasses: "DefaultRegisteredServiceUsernameProvider",
        labelTitle: "Scope",
        name: "registeredServiceUsernameAttributeScope",
        paramName: "usernameAttributeProvider.scope",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies the scope to be appended to the username."
    });

    CasDiscoveryProfile.fetchIfNeeded()
        .always(async () => {
            const options = CasDiscoveryProfile.availableAttributes().map(attr => ({
                value: attr,
                text: attr
            }));
            createMultiSelectField({
                cssClasses: "hide PrincipalAttributeRegisteredServiceUsernameProvider",
                singleSelect: true,
                containerId: "registeredServiceUsernameAttributeScopeFieldContainer",
                labelTitle: "Username Attribute:",
                paramName: "usernameAttributeProvider.usernameAttribute",
                title: "Specifies the principal attribute to be used as the username.",
                options: options,
                allowCreateOption: true,
                inclusion: "before"
            });

        });

    createInputField({
        cssClasses: "DefaultRegisteredServiceUsernameProvider",
        labelTitle: "Remove Pattern",
        name: "registeredServiceUsernameAttributeRemovePattern",
        paramName: "usernameAttributeProvider.removePattern",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies a regex pattern to remove from the username."
    }).after(`
       <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="validatePattern(this, '#registeredServiceUsernameAttributeRemovePattern')">
            <i class="mdi mdi-check-circle" aria-hidden="true"></i>
            <span class="sr-only">Validate</span>
        </button>
    `);

    createSelectField({
        cssClasses: "DefaultRegisteredServiceUsernameProvider",
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        labelTitle: "Canonicalization Mode:",
        paramName: "usernameAttributeProvider.canonicalizationMode",
        options: [
            {
                value: "",
                text: "NONE",
                selected: true
            },
            {
                value: "UPPER",
                text: "UPPER"
            },
            {
                value: "LOWER",
                text: "LOWER"
            }
        ],
        helpText: "Specifies the canonicalization mode for the username."
    });

    const generatorProviders = "PairwiseOidcRegisteredServiceUsernameAttributeProvider AnonymousRegisteredServiceUsernameAttributeProvider";
    createInputField({
        cssClasses: `hide ${generatorProviders}`,
        labelTitle: "Salt",
        name: "registeredServiceUsernameAttributeGeneratorSalt",
        paramName: "usernameAttributeProvider.persistentIdGenerator",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies the salt used to generate persistent or pairwise identifiers. A random salt is used when left blank."
    }).data("renderer", value => renderWizardPersistentIdGenerator("salt", value))
        .data("reader", generator => readWizardPersistentIdGenerator("salt", generator));

    createInputField({
        cssClasses: `hide ${generatorProviders}`,
        labelTitle: "Attribute",
        name: "registeredServiceUsernameAttributeGeneratorAttribute",
        paramName: "usernameAttributeProvider.persistentIdGenerator",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies the attribute whose value, instead of the principal id, the identifier is computed from."
    }).data("renderer", value => renderWizardPersistentIdGenerator("attribute", value))
        .data("reader", generator => readWizardPersistentIdGenerator("attribute", generator));
}

function createRegisteredServiceExpirationPolicy() {
    createInputField({
        cssClasses: "jquery-datepicker",
        data: {dateFormat: "yy-mm-dd"},
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy",
        labelTitle: "Expiration Date",
        name: "registeredServiceExpirationPolicyExpirationDate",
        paramName: "expirationPolicy.expirationDate",
        required: false,
        containerId: "editServiceWizardMenuItemExpirationPolicy",
        title: "Specifies the expiration date for the registered service in the format YYYY-MM-DD."
    });
}

/**
 * Put the field that says which requests an application answers, its service id, redirect URI or entity id, at the
 * top of the protocol section of the application's type, after the client id and secrets for OAuth and OpenID
 * Connect, since what it holds depends on the protocol. A type without a protocol section keeps it in the general
 * section.
 *
 * @param className the short name of the application's type
 */
function placeRegisteredServiceIdField(className) {
    const field = $("#registeredServiceIdFieldContainer");
    const containers = {
        CasRegisteredService: "#editServiceWizardCasContainer",
        OAuthRegisteredService: "#editServiceWizardOAuthOidcContainer",
        OidcRegisteredService: "#editServiceWizardOAuthOidcContainer",
        SamlRegisteredService: "#editServiceWizardSamlContainer",
        WSFederationRegisteredService: "#editServiceWizardWSFederationContainer"
    };
    const container = $(containers[className] ?? "");
    if (container.length === 0) {
        $("#registeredServiceIdHome").before(field);
        return;
    }
    const client = container.children("#registeredServiceClientSecretsContainer, #registeredServiceClientIdFieldContainer").last();
    if (client.length > 0) {
        client.after(field);
    } else {
        container.prepend(field);
    }
}

function createRegisteredServiceFields() {
    createInputField({
        labelTitle: "Name",
        name: "registeredServiceName",
        paramName: "name",
        required: true,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a logical name for this application, preferably without spaces or special characters."
    });

    createInputField({
        labelTitle: "Service ID",
        name: "registeredServiceId",
        paramName: "serviceId",
        required: true,
        dataType: "regex",
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a pattern that determines which requests are mapped to this application. This is typically a URL pattern or a regular expression."
    }).after(`
        <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="validatePattern(this)">
            <i class="mdi mdi-check-circle" aria-hidden="true"></i>
            <span class="sr-only">Validate</span>
        </button>
    `);

    $("#registeredServiceIdFieldContainer").after($("<span>", {id: "registeredServiceIdHome", hidden: true}));

    initializeDescriptionEditor("editServiceWizardGeneralContainer");

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Numeric ID",
        name: "registeredServiceIdentifier",
        paramName: "id",
        dataType: "number",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define a unique numeric identifier for this application. Depending on your choice of registry, this ID may be auto-generated."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Theme",
        name: "registeredServiceTheme",
        paramName: "theme",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a theme to customize the appearance of the login and other user interface pages for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Locale",
        name: "registeredServiceLocale",
        paramName: "locale",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a locale to customize the language and regional settings for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Information URL",
        name: "registeredServiceInformationUrl",
        paramName: "informationUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL that points to more information about this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Privacy URL",
        name: "registeredServicePrivacyUrl",
        paramName: "privacyUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL that points to the privacy policy for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Template",
        name: "registeredServiceTemplate",
        paramName: "templateName",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a template name to apply predefined settings and configurations for this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Evaluation Order",
        name: "registeredServiceEvaluationOrder",
        paramName: "evaluationOrder",
        dataType: "number",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the order in which this application is evaluated relative to other applications. Lower numbers indicate higher priority."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Logo",
        name: "registeredServiceEvaluationLogo",
        paramName: "logo",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a URL to a logo image that represents this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Logout URL",
        name: "registeredServiceEvaluationLogoutUrl",
        paramName: "logoutUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Specify a URL to redirect users to upon logout from this application."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Environment(s)",
        name: "registeredServiceEnvironments",
        paramName: "environments",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the environment(s), separated by comma, in which this application operates (e.g., development, staging, production)"
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });
}

/********************************************/

function hideAdvancedRegisteredServiceOptions() {
    const value = $("#hideAdvancedOptions").val();
    const className = getLastWord($("#serviceClassType").text());

    localStorage.setItem("hideAdvancedOptionsPreference", value);

    if (value === "true" || value === true) {
        hideElements($("form#editServiceWizardForm .advanced-option"));
    } else {
        showElements($("form#editServiceWizardForm .advanced-option"));
        hideElements($("form#editServiceWizardForm [class*='class-']").not(`.class-${className}`).not(".always-show"));
    }

    if (PalantirDashboardConfiguration.availableMultifactorProviders().length === 0) {
        hideElements($("#registeredServiceMfaPolicy"));
    }
    if (!CAS_FEATURES.includes("AcceptableUsagePolicy")) {
        hideElements($("#registeredServiceAcceptableUsagePolicy"));
    }
    if (!CAS_FEATURES.includes("InterruptNotifications")) {
        hideElements($("#registeredServiceWebflowInterruptPolicy"));
    }
    if (!CAS_FEATURES.includes("PasswordlessAuthn")) {
        hideElements($("#registeredServicePasswordlessPolicy"));
    }
    if (!CAS_FEATURES.includes("SurrogateAuthentication")) {
        hideElements($("#registeredServiceSurrogatePolicy"));
    }
    if (!CAS_FEATURES.some(feature => feature === "DelegatedAuthentication" || feature.startsWith("DelegatedAuthentication."))) {
        hideElements($("#registeredServiceDelegatedAuthenticationPolicy"));
    }
    RegisteredServiceSections.refresh();
}

function toggleJsonEditorVisibility() {
    const showEditor = $("#showJsonEditor").val();
    const $editorContainer = $("#wizardJsonEditorContainer");
    const $wizardMenu = $("#editServiceWizardMenu");

    localStorage.setItem("showJsonEditorPreference", showEditor);

    if (showEditor === "true" || showEditor === true) {
        $editorContainer.show();
        $wizardMenu.removeClass("w-100").addClass("mmw-45");
        const editor = ace.edit("wizardServiceEditor");
        if (editor) {
            editor.resize();
        }
    } else {
        $editorContainer.hide();
        $wizardMenu.removeClass("mmw-45").addClass("w-100");
    }

    RegisteredServiceSections.refresh();
}

/**
 * Whether a wizard field container is switched off for the type chosen in its section.
 *
 * Type handlers reveal their fields either through showElements, which drops the hide class, or
 * through jQuery's show(), which only sets an inline display and leaves the class in place. A
 * container is off when its inline display is none, or when it carries hide with no inline display
 * overriding it. What is on screen is not asked, since the wizard hides every section but the current
 * one, and a value entered in another section still belongs in the definition.
 *
 * @param element the field, select or map container
 * @returns {boolean} true when the container's values must stay out of the definition
 */
function isWizardContainerSwitchedOff(element) {
    const display = element.style.display;
    return display === "none" || (element.classList.contains("hide") && display === "");
}

/**
 * Whether a field holds text even when what is typed looks like a number, such as an OAuth client id, a service id, a
 * name or a secret: turning such a value into a number would change its type and, for long digits, the value itself.
 * Fields that hold numbers are number fields.
 *
 * @param $input the field
 * @param paramName the setting the field writes
 * @returns {boolean} true when the value is kept as text
 */
function isWizardTextSetting($input, paramName) {
    if (String($input.attr("type") ?? "").toLowerCase() === "number") {
        return false;
    }
    const setting = String(paramName ?? "").split(".").pop();
    return setting !== "id" && /(id|ids|name|secret|key|url|uri|location|realm|issuer|audience|pattern)$/i.test(setting);
}

let generateServiceDefinitionTimer = null;

/**
 * Generate the definition from the wizard's fields, shortly after the fields change. Calls made before it runs are
 * served by that one run, which reads the fields as they are then.
 */
function generateServiceDefinition() {
    if (isPopulatingWizard || generateServiceDefinitionTimer !== null) {
        return;
    }

    generateServiceDefinitionTimer = setTimeout(function () {
        generateServiceDefinitionTimer = null;
        if (!document.getElementById("wizardServiceEditor")) {
            return;
        }
        const editor = initializeAceEditor("wizardServiceEditor");
        const serviceDefinition = buildWizardServiceDefinition();
        editor.setValue(serviceDefinition === null ? "" : JSON.stringify(serviceDefinition, null, 4));
        editor.gotoLine(1);
        PalantirWizardValidity.refresh();
        PalantirWizardChains.render();
    }, 10);
}

/**
 * The definition the wizard's fields make, with each chain put back together around the member the fields hold.
 *
 * @returns {object|null} the definition, or null when the fields make nothing beyond the type of application
 */
function buildWizardServiceDefinition() {
    let serviceDefinition = {
        "@class": $("#serviceClassType").text().trim()
    };

    // Track which param names have been processed to avoid duplicates from hidden containers
    const processedMappedFields = new Set();

    // Get currently selected attribute release policy type for filtering
    const $attrReleasePolicySelect = $("select#registeredServiceAttributeReleasePolicy");
    const selectedAttrReleasePolicy = $attrReleasePolicySelect.length > 0 ? getLastWord($attrReleasePolicySelect.val()) : "";

    $("form#editServiceWizardForm")
        .find("input,select,textarea")
        .each(function () {
            const $input = $(this);
            const paramName = $input.data("param-name");
            const paramType = $input.data("param-type");
            const skipWhenFalse = $input.data("param-skip-false");
            const skipWhenTrue = $input.data("param-skip-true");

            // Skip inputs in MapContainers (handled specially)
            const $mapContainer = $input.closest("[id$='MapContainer']");
            if ($mapContainer.length > 0) {
                // For mapped fields, only process once per container
                const containerKey = `${paramName}:${$mapContainer.attr("id")}`;
                if (processedMappedFields.has(containerKey)) {
                    return; // Already processed this container
                }
                processedMappedFields.add(containerKey);

                // For attributeReleasePolicy.allowedAttributes, only process the container
                // that matches the currently selected policy type
                if (paramName === "attributeReleasePolicy.allowedAttributes") {
                    // Check if this container has the class matching the selected policy
                    if (selectedAttrReleasePolicy && !$mapContainer.hasClass(selectedAttrReleasePolicy)) {
                        return; // Skip containers for other policy types
                    }
                }

                // Check if this container has any non-empty values before processing
                const $keyInputs = $mapContainer.find("input[data-mapped-field-role='key']");
                const $allInputs = $mapContainer.find("input");
                let hasNonEmptyValue = false;
                if ($keyInputs.length > 0) {
                    for (let i = 0; i < $keyInputs.length; i++) {
                        const key = $($keyInputs[i]).val();
                        if (key && key.trim().length > 0) {
                            hasNonEmptyValue = true;
                            break;
                        }
                    }
                } else {
                    for (let i = 0; i < $allInputs.length; i += 2) {
                        const key = $($allInputs[i]).val();
                        if (key && key.trim().length > 0) {
                            hasNonEmptyValue = true;
                            break;
                        }
                    }
                }
                if (!hasNonEmptyValue) {
                    return; // Skip empty containers
                }
            } else {
                // For regular inputs/selects (not in MapContainer), check if they're in a hidden container
                // This handles fields like ReturnAllowedAttributeReleasePolicy's allowedAttributes
                const $fieldContainer = $input.closest("[id$='FieldContainer'], [id$='SelectContainer']");
                if ($fieldContainer.length > 0 && isWizardContainerSwitchedOff($fieldContainer[0])) {
                    // Don't skip if this is an advanced-option that has a value
                    if (!$fieldContainer.hasClass("advanced-option")) {
                        return; // Skip inputs in hidden field containers (except advanced-option)
                    }
                }

                // For attributeReleasePolicy.allowedAttributes, check if this field matches the selected policy
                if (paramName === "attributeReleasePolicy.allowedAttributes") {
                    if (selectedAttrReleasePolicy && $fieldContainer.length > 0 && !$fieldContainer.hasClass(selectedAttrReleasePolicy)) {
                        return; // Skip fields for other policy types
                    }
                }
            }

            let value = $input.val();

            if (value && Array.isArray(value)) {
                value = value.filter(v => v != null && v !== "").join(",");
            }
            if (paramName === "clientSecrets" && $input.attr("id") === "registeredServiceClientSecrets") {
                value = "clientSecrets";
            }
            if (paramName && paramName.trim().length > 0 && value && String(value).trim().length > 0) {
                if (skipWhenFalse && (value === "false" || value === false)) {
                    console.debug(`Skipping parameter ${paramName} because its value is false`);
                } else if (skipWhenTrue && (value === "true" || value === true)) {
                    console.debug(`Skipping parameter ${paramName} because its value is true`);
                } else {
                    const renderer = $input.data("renderer");

                    if (typeof renderer === "function") {
                        value = renderer(String(value), $input, serviceDefinition);
                    } else if (value === "true") {
                        value = true;
                    } else if (value === "false") {
                        value = false;
                    } else if (isNumeric(value) && !isWizardTextSetting($input, paramName)) {
                        value = Number(value);
                    }

                    if (paramName.includes(".")) {
                        const parts = paramName.split(".");
                        let current = serviceDefinition;

                        if (paramType && paramType.length > 0 && parts.length > 0) {
                            if (!current[parts[0]]) {
                                current[parts[0]] = {"@class": paramType};
                            }
                        }

                        for (let i = 0; i < parts.length; i++) {
                            let part = parts[i];
                            const isLastPart = (i === parts.length - 1);

                            if (isLastPart) {
                                // Always set the value at the last part of the path
                                if (typeof value === "object" && Object.keys(value).length > 0) {
                                    current[part] = mergeWizardPolicy(current[part], value);
                                } else if (typeof value !== "object") {
                                    current[part] = value;
                                }
                            } else {
                                // For intermediate parts, create empty object if it doesn't exist
                                if (!current[part]) {
                                    current[part] = {};
                                }
                                current = current[part];
                            }
                        }
                    } else {
                        serviceDefinition[paramName] = mergeWizardPolicy(serviceDefinition[paramName], value);
                    }
                }
            }
        });

    if (Object.keys(serviceDefinition).length > 1) {
        $("form#editServiceWizardForm")
            .find("input,select")
            .each(function () {
                const $input = $(this);
                const beforeGenerate = $input.data("beforeGenerate");
                if (typeof beforeGenerate === "function") {
                    beforeGenerate($input, serviceDefinition);
                }
            });

        const contacts = [...(wizardContactSlots.get(serviceDefinition) ?? new Map()).entries()]
            .sort(([left], [right]) => left - right)
            .map(([, contact]) => contact)
            .concat(wizardExtraContacts.map(contact => structuredClone(contact)));
        if (contacts.length > 0) {
            serviceDefinition.contacts = ["java.util.ArrayList", contacts];
        }
        PalantirWizardChains.pack(serviceDefinition);

        Object.keys(serviceDefinition).forEach(key => {
            if (
                key !== "@class" &&
                typeof serviceDefinition[key] === "object" &&
                serviceDefinition[key] !== null &&
                Object.keys(serviceDefinition[key]).length === 1 &&
                serviceDefinition[key].hasOwnProperty("@class")) {

                const classType = serviceDefinition[key]["@class"];
                const markerClass = $(`option[value='${classType}']`).data("markerClass");
                if (!markerClass) {
                    // console.log("Deleting", serviceDefinition[key]);
                    delete serviceDefinition[key];
                }
            }
        });
        const remainingKeys = Object.keys(serviceDefinition);
        return remainingKeys.length === 1 && remainingKeys[0] === "@class" ? null : serviceDefinition;
    }
    return null;
}

/**
 * Whether a switch shows as on. The switch itself is the truth: its hidden field is emptied whenever the wizard opens.
 *
 * @param $button the switch
 * @returns {boolean} true when it is on
 */
function wizardSwitchSelected($button) {
    return $button.attr("aria-checked") === "true" || $button.hasClass("mdc-switch--selected");
}

/**
 * Put every dropdown of the wizard back to the option it starts with and empty every multi-select, then let each
 * dropdown's handlers show and hide the fields that depend on it, so a definition opened after another one starts
 * from the same wizard a new one does.
 */
function resetWizardSelects() {
    $("#editServiceWizardForm select[data-param-name]").each(function () {
        if (this.tomselect) {
            this.tomselect.clear(true);
            return;
        }
        const options = [...this.options];
        const initial = options.find(option => option.defaultSelected) ?? options[0];
        if (initial) {
            this.value = initial.value;
        }
    });
    $("#editServiceWizardForm select.jqueryui-selectmenu[data-param-name]").each(function () {
        const $select = $(this);
        String($select.data("change-handler") ?? "").split(",")
            .filter(name => name.length > 0 && name !== "generateServiceDefinition" && typeof window[name] === "function")
            .forEach(name => window[name]($select));
    });
}

/**
 * Put every switch of the wizard, or of one part of it, back the way the page first drew it, before a definition fills the wizard in, so a
 * switch turned on or off for the previous application does not show for the next one, or for the policy chosen
 * after another. Its hidden field stays empty,
 * so a switch nobody touches adds nothing to the definition. The hidden field's value is its default only until the
 * switch is first used, so the default is noted the first time the wizard opens.
 *
 * @param scope the part of the wizard whose switches are put back
 */
function resetWizardSwitches(scope = "#editServiceWizardForm") {
    $(scope).find("input[type='hidden'][data-switch-btn]").each(function () {
        if (this.dataset.switchDefault === undefined) {
            this.dataset.switchDefault = String(this.getAttribute("value") === "true");
        }
        const $button = $(`#${this.dataset.switchBtn}`);
        if ($button.length > 0 && wizardSwitchSelected($button) !== (this.dataset.switchDefault === "true")) {
            $button.click();
        }
        this.value = "";
    });
}

/**
 * The value a field gives a top-level setting, keeping what other fields already put in it. A policy is chosen by
 * a list whose value names the policy's type, while switches and fields elsewhere in the section set the policy's
 * other settings, such as whether authentication attributes may be released. Whichever is read first, the type
 * and the other settings end up in one policy rather than the type replacing them.
 *
 * @param current what the setting holds so far
 * @param value   the value the field gives it
 * @returns {*} the setting's value
 */
function mergeWizardPolicy(current, value) {
    const isPolicy = candidate => candidate !== null && typeof candidate === "object" && !Array.isArray(candidate);
    if (!isPolicy(current) || !isPolicy(value)) {
        return value;
    }
    const settings = Object.fromEntries(Object.entries(current).filter(([key]) => key !== "@class"));
    return {...value, ...settings};
}

/**
 * The required and otherwise constrained fields of the wizard, as they apply to the application type being edited:
 * Save stays disabled, with the missing fields named beside it, until every one of them holds a valid value, so an
 * incomplete definition never reaches the server.
 */
class PalantirWizardValidity {
    /**
     * The fields that apply: those in the sections this type offers, outside parts hidden because they belong to
     * other types or choices. Fields of the other sections count even though only one section shows at a time.
     *
     * @returns {HTMLElement[]} the fields
     */
    static fields() {
        return RegisteredServiceSections.sections()
            .filter(RegisteredServiceSections.isAvailable)
            .flatMap(section => $(section.panel).find("input, select, textarea").toArray()
                .filter(field => field.willValidate && !PalantirWizardValidity.hiddenWithin(field, section.panel))
                .map(field => ({field, section})));
    }

    static hiddenWithin(field, panel) {
        return [field, ...$(field).parentsUntil(panel).toArray()]
            .some(node => node.classList.contains("hide") || node.classList.contains("d-none") || node.style.display === "none");
    }

    /**
     * The fields that hold no valid value yet, with the section each is in.
     *
     * @returns {Array<{field: HTMLElement, section: object}>} the invalid fields
     */
    static invalid() {
        return PalantirWizardValidity.fields().filter(entry => !entry.field.validity.valid);
    }

    /**
     * How a field is named on screen: its label, or failing that its placeholder or name.
     *
     * @param field the field
     * @returns {string} the name
     */
    static labelOf(field) {
        const label = field.id ? document.querySelector(`label[for="${CSS.escape(field.id)}"]`) : null;
        const text = (label?.querySelector(".mdc-floating-label")?.textContent ?? label?.textContent ?? field.getAttribute("placeholder") ?? field.name ?? "")
            .replace(/\*/g, "").replace(/\s+/g, " ").trim();
        return text || "A field";
    }

    /**
     * Enable Save only when every field that applies is valid, and name the fields still missing.
     */
    static refresh() {
        const dialog = $("#editServiceWizardDialog");
        if (!dialog.hasClass("ui-dialog-content") || !dialog.dialog("isOpen")) {
            return;
        }
        const invalid = PalantirWizardValidity.invalid();
        const names = [...new Set(invalid.map(entry => PalantirWizardValidity.labelOf(entry.field)))];
        const writable = typeof CasActuatorEndpoints.writable !== "function" || CasActuatorEndpoints.writable("registeredServices");
        $("#saveServiceWizard").prop("disabled", invalid.length > 0 || !writable);
        const hint = $("#saveServiceWizardHint");
        if (names.length === 0) {
            hint.prop("hidden", true).empty();
        } else {
            const shown = names.slice(0, 3).join(", ");
            hint.prop("hidden", false)
                .attr("title", names.join(", "))
                .html(`<i class="mdi mdi-alert-circle-outline" aria-hidden="true"></i><span>Required: ${escapePalantirMarkup(shown)}${names.length > 3 ? ` and ${names.length - 3} more` : ""}</span>`);
        }
    }

    /**
     * Open the section of the first invalid field and show what is wrong with it.
     *
     * @returns {boolean} true when every field is valid
     */
    static report() {
        const first = PalantirWizardValidity.invalid()[0];
        if (!first) {
            return true;
        }
        RegisteredServiceSections.activate(first.section.key);
        RegisteredServiceSections.render?.();
        setTimeout(() => {
            first.field.scrollIntoView({block: "center", behavior: "smooth"});
            first.field.reportValidity();
        }, 0);
        return false;
    }
}

function generateMappedFieldValue(sectionId, config) {
    const {
        multipleValues = false,
        valueFieldRenderer,
        multipleValuesType = "java.util.ArrayList",
        unwrapSingleElement = false,
        groovyEditorTitle = "",
        mapType = "java.util.TreeMap"
    } = config;

    const definition = {};

    const rows = $(`#${sectionId}`).find("[data-mapped-input-row='true']");
    rows.each(function () {
        const $row = $(this);
        const $keyInput = $row.find("input[data-mapped-field-role='key']").first();
        const $valueInput = $row.find("input[data-mapped-field-role='value']").first();
        const key = $keyInput.val();
        const value = $valueInput.val();

        // Skip empty keys
        if (!key || key.trim().length === 0) {
            return;
        }

        if (valueFieldRenderer !== undefined && typeof valueFieldRenderer === "function") {
            const additionalInputs = {};
            $row.find("input[data-mapped-field-role]")
                .not("[data-mapped-field-role='key']")
                .not("[data-mapped-field-role='value']")
                .each(function () {
                    const fieldName = $(this).data("mapped-field-role");
                    additionalInputs[fieldName] = $(this);
                });
            definition[key] = valueFieldRenderer($keyInput, $valueInput, additionalInputs, $row);
        } else if (value && value.trim().length > 0) {
            if (groovyEditorTitle && /^groovy\s*\{[\s\S]*\}\s*$/.test(value.trim())) {
                definition[key] = value.trim();
            } else if (multipleValues) {
                const valueArray = value.split(",").filter(entry => entry.trim().length > 0);
                if (unwrapSingleElement && valueArray.length === 1) {
                    definition[key] = value.trim();
                } else {
                    definition[key] = [multipleValuesType, valueArray];
                }
            } else {
                const given = $valueInput.attr("data-mapped-value-key") === key ? $valueInput.attr("data-mapped-value-type") : undefined;
                definition[key] = mappedFieldValue(value, given);
            }
        }
    });
    return {"@class": mapType, ...definition};
}

/**
 * The value of a mapped field as the definition held it: a number, a boolean, a list or an object the wizard was given
 * goes back as one, so long as the text still reads as one and the row still has the key it was given with; a row
 * cleared and filled in again starts as text.
 *
 * @param text the text of the field
 * @param type the type of the value the field was given: number, boolean or json, when it was not text
 * @returns {string|number|boolean|object|Array} the value
 */
function mappedFieldValue(text, type) {
    const trimmed = text.trim();
    if (type === "json") {
        try {
            const parsed = JSON.parse(trimmed);
            if (parsed !== null && typeof parsed === "object") {
                return parsed;
            }
        } catch (error) {
            return text;
        }
    }
    if (type === "boolean" && /^(true|false)$/i.test(trimmed)) {
        return trimmed.toLowerCase() === "true";
    }
    if (type === "number" && trimmed.length > 0 && Number.isFinite(Number(trimmed))) {
        return Number(trimmed);
    }
    return text;
}

/**
 * Attach the Material text fields under an element, such as a row just added to a mapped field. Attaching every text
 * field of the page instead costs more as the wizard grows and adds another set of listeners to each field every time.
 *
 * @param scope the element to look under
 */
function attachPalantirTextFields(scope) {
    $(scope).find(".mdc-text-field").addBack(".mdc-text-field").each((index, field) => {
        mdc.textField.MDCTextField.attachTo(field);
    });
}

function createMappedInputField(config) {
    const {
        header = "",
        cssClasses = "",
        keyField,
        keyLabel = "",
        valueField,
        valueFieldType = "text",
        valueLabel = "",
        containerId,
        containerField,
        containerType,
        required = false,
        multipleValues = false,
        multipleValuesType = "java.util.ArrayList",
        valueFieldRenderer,
        unwrapSingleElement = false,
        onChangeCallback,
        groovyEditorTitle = "",
        autoComplete = [],
        additionalFields = []
    } = config;

    const changeCallback = onChangeCallback || generateServiceDefinition;

    const sectionContainerId = `registeredService${capitalize(keyField)}MapContainer`;
    const addButtonId = `registeredService${capitalize(keyField)}AddButton`;
    const deleteAllButtonId = `registeredService${capitalize(keyField)}DeleteAllButton`;
    const removeButtonId = `registeredService${capitalize(keyField)}RemoveButton`;
    const mapRowId = `registeredService${capitalize(keyField)}Row`;

    const inputFieldKeyId = `registeredService${capitalize(keyField)}`;
    const inputFieldValueId = `registeredService${capitalize(valueField)}`;
    const additionalFieldElements = additionalFields.map(field => {
        const inputFieldId = `registeredService${capitalize(field.field)}`;
        return `
                <label for="${inputFieldId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses} ${field.cssClasses ?? ""}">
                    <span class="mdc-notched-outline">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${field.label ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input form-control"
                           id="${inputFieldId}"
                           name="${inputFieldId}"
                           size="25"
                           type="${field.type ?? "text"}"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="${field.name}"
                           ${required ? "required" : ""}/>
                </label>
        `;
    }).join("");
    const rowElements = `
            <div class="d-flex justify-content-between pt-2 ${keyField}-map-row ${cssClasses}" id="${mapRowId}" data-mapped-input-row="true">
                <label for="${inputFieldKeyId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses}">
                    <span class="mdc-notched-outline pr-2">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${keyLabel ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input"
                           id="${inputFieldKeyId}"
                           name="${inputFieldKeyId}"
                           type="text"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="key"
                           ${required ? "required" : ""}/>
                </label>

                <label for="${inputFieldValueId}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses}">
                    <span class="mdc-notched-outline">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${valueLabel ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input form-control"
                           id="${inputFieldValueId}"
                           name="${inputFieldValueId}"
                           size="25"
                           type="${valueFieldType}"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           data-mapped-field-role="value"
                           ${!multipleValues && typeof valueFieldRenderer !== "function" ? "data-mapped-plain=\"true\"" : ""}
                           ${required ? "required" : ""}/>
                </label>

                ${additionalFieldElements}

                <button type="button"
                        id="${removeButtonId}"
                        name="${removeButtonId}"
                        title="Remove Row"
                        class="mdc-button mdc-button--raised btn btn-link mdc-button--inline-row ${cssClasses}">
                    <i class="mdi mdi-minus-thick" aria-hidden="true"></i>
                </button>
            </div>
    `;

    const html =
        `<div id="${sectionContainerId}" class="${cssClasses}">
            <h3 class="mt-2 mb-2 ${header && header.length > 0 ? "" : "hide"} ${cssClasses}">${header}</h3>
            ${rowElements}
            <span id="${sectionContainerId}ToAppend" class="pt-2 ${cssClasses}"></span>
            <div class="d-flex pt-2 ${cssClasses}">
                <button type="button" 
                        name="${addButtonId}"
                        id="${addButtonId}"
                        title="Add Row"
                        class="mdc-button mdc-button--raised mdc-button--round add-row ${cssClasses}">
                    <span class="mdc-button__label">
                        <i class="mdc-tab__icon mdi mdi-plus-thick" aria-hidden="true"></i>
                    </span>
                </button>
                <button type="button" 
                        name="${deleteAllButtonId}"
                        id="${deleteAllButtonId}"
                        title="Delete All Rows"
                        class="mdc-button mdc-button--raised mdc-button--round add-row ${cssClasses}">
                    <span class="mdc-button__label">
                        <i class="mdc-tab__icon mdi mdi-trash-can" aria-hidden="true"></i>
                    </span>
                </button>
            </div>
        </div>
        `;

    const section = $(`${html}`);
    decoratePalantirInputIcons(section[0]);
    $(`#${containerId}`).append(section);

    function configureRemoveMapRowEventHandler() {
        $(`button[name=${removeButtonId}]`).off().on("click", function () {
            $(this).closest(`.${keyField}-map-row`).remove();
            changeCallback();
        });
    }

    function configureInputEventHandler() {
        $(`#${sectionContainerId} input`).off().on("input", () => {
            changeCallback();
        });
    }

    function configureInputRenderer() {
        $(`#${sectionContainerId} input`).data("renderer", function () {
            return generateMappedFieldValue(sectionContainerId, config);
        });
    }

    function configureGroovyEditors(scope) {
        const editorFields = [];
        if (groovyEditorTitle) {
            editorFields.push({role: "value", title: groovyEditorTitle});
        }
        additionalFields.filter(field => field.groovyEditorTitle).forEach(field => {
            editorFields.push({role: field.name, title: field.groovyEditorTitle});
        });
        editorFields.forEach(editorField => {
            $(scope).find(`input[data-mapped-field-role='${editorField.role}']`).each(function () {
                attachPalantirGroovyEditorButton({
                    target: this,
                    title: editorField.title
                });
            });
        });
    }

    $(`button[name=${addButtonId}]`).off().on("click", () => {
        let elementsToAdd = $(rowElements).removeClass("hide");
        elementsToAdd.find("*").removeClass("hide");

        decoratePalantirInputIcons(elementsToAdd[0]);
        $(`#${sectionContainerId}ToAppend`).append(elementsToAdd);
        configureGroovyEditors(elementsToAdd);
        configureRemoveMapRowEventHandler();
        attachPalantirTextFields(elementsToAdd);
        configureInputEventHandler();
        configureInputRenderer();
    });
    $(`button[name=${deleteAllButtonId}]`).off().on("click", () => {
        $(`#${sectionContainerId}ToAppend`).empty();
        $(`#${sectionContainerId} input`).val("").removeAttr("data-mapped-value-type data-mapped-value-key");
        configureRemoveMapRowEventHandler();
        configureInputEventHandler();
        configureInputRenderer();
        changeCallback();
    });

    configureRemoveMapRowEventHandler();
    configureInputEventHandler();
    configureInputRenderer();
    configureGroovyEditors($(`#${sectionContainerId}`));

    if (autoComplete && autoComplete.length > 0) {
       $(`#${inputFieldKeyId}`).autocomplete({
           delay: 0,
           source: autoComplete,
           open: function( event, ui ) {
                attachPalantirTextFields($(this).closest(".mdc-text-field"));
           },
           select: function (event, ui) {
                $(this).val(ui.item.value);
                attachPalantirTextFields($(this).closest(".mdc-text-field"));
                return false;
           }
       });
    }
    return $(`#${sectionContainerId}`);
}

function appendOptionsToDropDown(config) {
    const {
        selectElement,
        options
    } = config;

    if (options) {
        options.forEach(opt => {
            const $opt = $("<option>")
                .attr("value", opt.value)
                .text(opt.text);
            if (opt.data && Object.keys(opt.data).length > 0) {
                Object.entries(opt.data).forEach(([key, value]) => {
                    $opt.data(key, value);
                });
            }
            if (opt.selected) {
                $opt.attr("selected", "selected");
            }

            selectElement.append($opt);
        });
    }
}

function createSelectField(config) {
    const {
        containerId,
        labelTitle,
        paramName,
        paramType = "",
        options,
        serviceClass = "",
        cssClasses = "",
        labelCssClasses = "",
        changeEventHandlers = "",
        id = ""
    } = config;

    let selectId = "";
    if (id !== undefined && id.trim().length > 0) {
        selectId = id.replace(".", "").trim();
    } else {
        selectId = `registeredService${capitalize(paramName.replace(".", ""))}`;
    }

    const $label = $("<label>")
        .addClass(serviceClass ?? "")
        .addClass("pt-2")
        .addClass("mb-2")
        .addClass(cssClasses ?? "")
        .addClass(labelCssClasses ?? "")
        .css("display", "block")
        .attr("for", selectId).text(`${labelTitle} `);

    const $select = $("<select>")
        .attr("id", selectId)
        .attr("data-change-handler", `${`${changeEventHandlers},`}generateServiceDefinition`)
        .attr("data-param-name", paramName)
        .attr("data-param-type", paramType)
        .addClass("jqueryui-selectmenu");

    appendOptionsToDropDown({
        selectElement: $select,
        options: options
    });

    $label.append($select);

    const container = $("<div>", {
        id: `${selectId}SelectContainer`,
        class: `${serviceClass ?? ""} ${cssClasses ?? ""}`
    });
    $(container).append($label);

    if (typeof containerId === "string") {
        $(`#${containerId}`).append(container);
    } else {
        $(containerId).append(container);
    }
    return $select;
}

function createMultiSelectField(config) {
    const {
        containerId,
        paramName,
        paramType = "",
        options,
        serviceClass = "",
        cssClasses = "",
        changeEventHandlers = "",
        labelTitle,
        allowCreateOption = false,
        singleSelect = false,
        inclusion = "append",
        id
    } = config;

    let selectId = "";
    if (id !== undefined && id.trim().length > 0) {
        selectId = id.replace(".", "").trim();
    } else {
        selectId = `registeredService${capitalize(paramName.replace(".", ""))}`;
    }

    const $select = $(`<select ${singleSelect ? "" : "multiple"}>`)
        .attr("id", selectId)
        .attr("data-change-handler", `${`${changeEventHandlers},`}generateServiceDefinition`)
        .attr("data-param-name", paramName)
        .attr("data-param-type", paramType)
        .addClass("jqueryui-multiselectmenu")
        .addClass(serviceClass ?? "")
        .addClass(cssClasses ?? "");

    appendOptionsToDropDown({
        selectElement: $select,
        options: options
    });
    const container = $("<span>", {
        id: `${selectId}SelectContainer`,
        class: `d-flex mb-2 mt-2 ${serviceClass ?? ""} ${cssClasses ?? ""}`
    });

    const $label = $("<label>")
        .addClass(serviceClass ?? "")
        .addClass("pt-2")
        .addClass("pr-2")
        .addClass("mb-2")
        .addClass(cssClasses ?? "")
        .css("min-width", "max-content")
        .attr("for", selectId).text(`${labelTitle} `);

    container.append($label, $select);

    if (inclusion === undefined || inclusion === "" || inclusion === "append") {
        $(`#${containerId}`).append(container);
    } else if (inclusion === "prepend") {
        $(`#${containerId}`).prepend(container);
    } else if (inclusion === "before") {
        $(`#${containerId}`).before(container);
    } else if (inclusion === "after") {
        $(`#${containerId}`).after(container);
    }

    let settings = {
        dropdownParent: $select.closest("#editServiceWizardDialog").length > 0 ? null : "body",
        plugins: ["remove_button"],
        onItemAdd(value, item) {
            generateServiceDefinition();
        },
        onItemRemove(value) {
            generateServiceDefinition();
        }
    };
    if (allowCreateOption) {
        settings.create = true;
        settings.persist = false;
    }
    new TomSelect(`#${selectId}`, settings);

    return $select;
}

function initializeDescriptionEditor(containerId) {
    setTimeout(function () {
        const editorContainer = $("<div>", {
            id: "registeredServiceDescriptionEditorContainer",
            class: "mb-2"
        });
        
        const editorTextarea = $("<textarea>", {
            id: "registeredServiceDescriptionEditor",
            name: "registeredServiceDescriptionEditor",
            "data-param-name": "description",
            title: "Provide a brief description of the application."
        });

        editorContainer.append(editorTextarea);
        $(`#${containerId}`).append(editorContainer);

        if (PalantirDashboardConfiguration.trumbowygIconsPath()) {
            $.trumbowyg.svgPath = PalantirDashboardConfiguration.trumbowygIconsPath();
        }

        $("#registeredServiceDescriptionEditor").trumbowyg({
            btns: [["bold", "italic", "underline", "removeformat", "viewHTML"]],
            removeformatPasted: true,
            semantic: false,
            autogrow: true,
            defaultLinkTarget: "_blank",
            autogrowOnEnter: true
        }).on("tbwchange tbwblur", function () {
            generateServiceDefinition();
        });
        $("#registeredServiceDescriptionEditor").attr("aria-label", "Description").data("renderer", unwrapDescriptionParagraph);
        $("#registeredServiceDescriptionEditorContainer .trumbowyg-editor")
            .attr({"role": "textbox", "aria-multiline": "true", "aria-label": "Description"});
    }, 150);
}

/**
 * The editor keeps every line in a paragraph; a description that is a single paragraph is kept without that wrapper,
 * so a plain description stays plain text, while several paragraphs keep their markup.
 *
 * @param value the HTML of the editor
 * @returns {string} the description
 */
function unwrapDescriptionParagraph(value) {
    const body = new DOMParser().parseFromString(String(value ?? ""), "text/html").body;
    const nodes = [...body.childNodes].filter(node => node.nodeType !== Node.TEXT_NODE || node.textContent.trim().length > 0);
    if (nodes.length === 1 && nodes[0].nodeName === "P" && nodes[0].attributes.length === 0) {
        const inner = nodes[0].innerHTML.trim();
        return inner.length > 0 && inner !== "<br>" ? inner : String(value);
    }
    return String(value);
}

let descriptionEditorValue = "";

let descriptionEditorTimer = null;

/**
 * Put a description in the description editor. Until the editor is ready, only the latest description is kept and
 * applied once it is, and the definition is generated again then, since it was generated without it.
 *
 * @param value the description
 */
function setDescriptionEditorValue(value) {
    descriptionEditorValue = value || "";
    const $editor = $("#registeredServiceDescriptionEditor");
    if ($editor.length > 0 && $editor.data("trumbowyg")) {
        clearTimeout(descriptionEditorTimer);
        descriptionEditorTimer = null;
        $editor.trumbowyg("html", descriptionEditorValue);
        return;
    }
    if (descriptionEditorTimer === null) {
        descriptionEditorTimer = setTimeout(() => {
            descriptionEditorTimer = null;
            setDescriptionEditorValue(descriptionEditorValue);
            if (!isPopulatingWizard) {
                generateServiceDefinition();
            }
        }, 150);
    }
}

function createInputField(config) {
    const {
        labelTitle,
        name,
        paramName,
        paramType = "",
        required,
        dataType = "text",
        containerId,
        inclusion = "append",
        title,
        serviceClass = "",
        cssClasses = "",
        data = {}
    } = config;

    const label = $("<label>", {
        for: name,
        id: `${name}Label`,
        class: `${serviceClass ?? ""} mdc-text-field mdc-text-field--outlined control-label mdc-text-field--with-trailing-icon mb-2 ${cssClasses ?? ""}`
    });

    const outline = $("<span>", {class: "mdc-notched-outline"});
    outline.append($("<span>", {class: "mdc-notched-outline__leading"}));

    const notch = $("<span>", {class: "mdc-notched-outline__notch"});
    notch.append($("<span>", {id: `${name}LabelText`, class: "mdc-floating-label", html: labelTitle}));

    outline.append(notch);
    outline.append($("<span>", {class: "mdc-notched-outline__trailing"}));

    const input = $("<input>", {
        class: `${serviceClass ?? ""} mdc-text-field__input form-control ${cssClasses ?? ""}`,
        type: dataType === "regex" || dataType === "text" ? "text" : dataType,
        id: name,
        name: name,
        "data-param-name": paramName,
        "data-param-type": paramType,
        tabindex: 0,
        size: 50,
        title: title,
        required: required
    }).data("tooltip-html", title).on("input", function () {
        const value = $(this).val();
        if (dataType === "date") {
            if (value.length === 0) {
                $(`${name}LabelText`).text(title);
            } else {
                $(`${name}LabelText`).text("");
            }
            generateServiceDefinition();
        } else if (value.length > 0 && dataType === "regex" && !isValidRegex(value)) {
            this.setCustomValidity("Value must be a valid regular expression.");
        } else if (required && value.length <= 0) {
            this.setCustomValidity("Field value is required");
        } else {
            this.setCustomValidity("");
            generateServiceDefinition();
        }
    })
        .on("blur", function () {
            // Skip validation styling during wizard population
            if (isPopulatingWizard) {
                return;
            }
            if (!this.checkValidity()) {
                $(this).parent().addClass("missing-required-field");
            } else {
                $(this).parent().removeClass("missing-required-field");
            }
        });

    Object.entries(data).forEach(([key, value]) => {
        input.data(key, value);
    });
    label.append(outline, input);
    decoratePalantirInputIcons(label[0]);

    const container = $("<span>", {
        id: `${name}FieldContainer`,
        class: `${serviceClass ?? ""} ${cssClasses ?? ""}`
    });

    $(container).append(label);
    if (inclusion === undefined || inclusion === "" || inclusion === "append") {
        if (typeof containerId === "string") {
            $(`#${containerId}`).append(container);
        } else {
            $(containerId).append(container);
        }
    } else if (inclusion === "prepend") {
        if (typeof containerId === "string") {
            $(`#${containerId}`).prepend(container);
        } else {
            $(containerId).prepend(container);
        }
    }
    return input;
}

// Flag to prevent generateServiceDefinition from running during population
let isPopulatingWizard = false;

/**
 * Populates wizard fields from an existing service definition.
 * Handles all field types: input fields, select fields, switch buttons,
 * multi-select fields, and mapped fields.
 * @param {Object} serviceDefinition - The service definition JSON object
 */
function populateWizardFromServiceDefinition(definition, options = {}) {
    const serviceDefinition = PalantirWizardChains.unpack(definition);
    const $wizardDialog = $("#editServiceWizardDialog").closest(".ui-dialog");
    $wizardDialog.addClass("is-populating").attr("aria-busy", "true");

    // Set flag to prevent generateServiceDefinition from running during population
    isPopulatingWizard = true;

    /**
     * Extracts a nested value from an object using dot notation path
     * @param {Object} obj - The object to extract from
     * @param {string} path - Dot notation path (e.g., "accessStrategy.enabled")
     * @returns {*} The value at the path, or undefined if not found
     */
    function getNestedValue(obj, path) {
        if (!obj || !path) {
            return undefined;
        }
        const parts = path.split(".");
        let current = obj;
        for (const part of parts) {
            if (current === null || current === undefined) {
                return undefined;
            }
            current = current[part];
        }
        return current;
    }

    /**
     * Converts array values to comma-separated strings
     * Handles Java collection wrappers like ["java.util.HashSet", [...]]
     * @param {*} value - The value to convert
     * @returns {string} Comma-separated string or original value as string
     */
    function arrayToString(value) {
        if (Array.isArray(value)) {
            if (value.length === 2 && typeof value[0] === "string" && value[0].startsWith("java.util.") && Array.isArray(value[1])) {
                return value[1].join(",");
            }
            return value.join(",");
        }
        return value !== null && value !== undefined ? String(value) : "";
    }

    /**
     * Properly floats the MDC text field label when a value is set programmatically
     * Also removes the missing-required-field class and clears validation errors
     * @param {jQuery} $input - The input element
     */
    function floatMdcTextFieldLabel($input) {
        const textFieldEl = $input.closest(".mdc-text-field")[0];
        if (textFieldEl) {
            // Add the floating label class
            textFieldEl.classList.add("mdc-text-field--label-floating");

            // Handle the notched outline
            const notchedOutline = textFieldEl.querySelector(".mdc-notched-outline");
            if (notchedOutline) {
                notchedOutline.classList.add("mdc-notched-outline--notched");
            }

            // Re-attach the MDC text field to update its state
            try {
                const mdcTextField = mdc.textField.MDCTextField.attachTo(textFieldEl);
                // Force layout update
                mdcTextField.layout();
            } catch (e) {
                // Component may already be attached, which is fine
            }
        }

        // Remove the missing-required-field class from parent if value is set
        if ($input.val() && $input.val().length > 0) {
            $input.parent().removeClass("missing-required-field");
            // Clear any custom validity
            if ($input[0] && $input[0].setCustomValidity) {
                $input[0].setCustomValidity("");
            }
        }
    }

    /**
     * Updates a select field's value and triggers change handlers
     * @param {jQuery} $select - The select element
     * @param {string} value - The value to set
     */
    function setSelectValue($select, value) {
        if (!$select.length || value === undefined || value === null) {
            return;
        }

        // Convert booleans to strings
        let stringValue = value;
        if (typeof value === "boolean") {
            stringValue = value.toString();
        }

        // Try to find the option with exact match, then uppercase, then lowercase
        let $option = $select.find(`option[value="${stringValue}"]`);
        if ($option.length === 0) {
            $option = $select.find(`option[value="${stringValue.toString().toUpperCase()}"]`);
        }
        if ($option.length === 0) {
            $option = $select.find(`option[value="${stringValue.toString().toLowerCase()}"]`);
        }
        if ($option.length === 0) {
            $option = $select.find("option").filter((index, option) => option.text === String(stringValue));
        }

        if ($option.length > 0) {
            $select.val($option.val());
            try {
                $select.selectmenu("refresh");
            } catch (e) {
                // Selectmenu not initialized yet
            }

            const handlerNames = $select.data("change-handler");
            if (handlerNames) {
                const handlers = handlerNames.split(",");
                for (const handlerName of handlers) {
                    if (handlerName && handlerName.length > 0 && handlerName !== "generateServiceDefinition" && typeof window[handlerName] === "function") {
                        window[handlerName]($select);
                    }
                }
            }
        }
    }

    /**
     * Updates a TomSelect multi-select field
     * @param {jQuery} $select - The select element
     * @param {Array|string} values - The values to set
     */
    function setMultiSelectValue($select, values) {
        if (!$select.length) {
            return;
        }

        const tomSelect = $select[0].tomselect;
        if (tomSelect) {
            tomSelect.clear();
            const valueArray = Array.isArray(values) ? values : (values ? values.split(",") : []);
            for (const val of valueArray) {
                if (val && val.trim().length > 0) {
                    if (!tomSelect.options[val.trim()]) {
                        tomSelect.addOption({value: val.trim(), text: val.trim()});
                    }
                    tomSelect.addItem(val.trim());
                }
            }
        }
    }

    /**
     * Updates a switch button (checkbox) field
     * @param {string} buttonId - The button ID (without Button suffix)
     * @param {boolean} value - The value to set
     */
    function setSwitchButtonValue(buttonId, value) {
        const $input = $(`input#${buttonId}`);
        const $button = $(`button#${buttonId}Button`);

        if (!$input.length) {
            return;
        }

        const newValue = value === true || value === "true" || value === "TRUE";
        if (wizardSwitchSelected($button) !== newValue) {
            $button.click();
        }
        $input.val(String(newValue));
    }

    /**
     * Populates a mapped input field section (key-value pairs)
     * @param {string} containerFieldName - The field name for the container
     * @param {Object} mapData - The map data object
     * @param {string} policyClassName - Optional: the policy class name to find the correct container
     */
    function populateMappedField(containerFieldName, mapData, policyClassName = null) {
        if (!mapData || typeof mapData !== "object") {
            return;
        }

        const entries = Object.entries(mapData).filter(([key]) => key !== "@class");
        if (entries.length === 0) {
            return;
        }

        // Find inputs matching the param name
        let $inputs = $(`form#editServiceWizardForm input[data-param-name="${containerFieldName}"]`);
        if ($inputs.length === 0) {
            return;
        }

        // If a policy class name is provided, find the container that matches
        let $container = null;
        if (policyClassName) {
            const policyShortName = getLastWord(policyClassName);
            // Find a container that has the policy class in its CSS classes
            $inputs.each(function () {
                const $potentialContainer = $(this).closest("[id$='MapContainer']");
                if ($potentialContainer.hasClass(policyShortName) ||
                    $potentialContainer.find(`.${policyShortName}`).length > 0 ||
                    $potentialContainer.closest(`.${policyShortName}`).length > 0) {
                    $container = $potentialContainer;
                    return false; // break
                }
            });
        }

        // Fallback: find a policy-enabled container or the first container
        if (!$container || !$container.length) {
            const $firstKeyInput = $inputs.first();
            $container = $firstKeyInput.closest("[id$='MapContainer']");

            // Inactive sidebar sections may hide an otherwise enabled container
            $inputs.each(function () {
                const $potentialContainer = $(this).closest("[id$='MapContainer']");
                if (!isWizardContainerSwitchedOff($potentialContainer[0])) {
                    $container = $potentialContainer;
                    return false; // break
                }
            });
        }

        if (!$container || !$container.length) {
            return;
        }

        const $addButton = $container.find("button[name$='AddButton']");

        function mapFieldValueToString(value, role) {
            if (typeof value === "object" && value !== null && !Array.isArray(value)) {
                if (role && role !== "value") {
                    return value[role] !== undefined ? arrayToString(value[role]) : "";
                }
                if (value["@class"]) {
                    if (value.pattern) {
                        return value.pattern;
                    }
                    if (value.values) {
                        return arrayToString(value.values);
                    }
                    return JSON.stringify(value);
                }
                return arrayToString(value);
            }
            return arrayToString(value);
        }

        entries.forEach(([key, value], index) => {
            if (index > 0 && $addButton.length) {
                $addButton.click();
            }

            const $row = $container.find("[data-mapped-input-row='true']").eq(index);
            if ($row.length > 0) {

                const $keyInput = $row.find(`input[data-param-name="${containerFieldName}"][data-mapped-field-role='key']`).first();
                if ($keyInput.length > 0) {
                    $keyInput.val(key).trigger("input");
                    floatMdcTextFieldLabel($keyInput);
                }

                $row.find(`input[data-param-name="${containerFieldName}"][data-mapped-field-role]`)
                    .not("[data-mapped-field-role='key']")
                    .each(function () {
                        const $input = $(this);
                        const role = $input.data("mapped-field-role");
                        const structured = role === "value" && $input.attr("data-mapped-plain") === "true"
                            && value !== null && typeof value === "object";
                        const valueStr = structured ? JSON.stringify(value) : mapFieldValueToString(value, role);
                        if (structured) {
                            $input.attr({"data-mapped-value-type": "json", "data-mapped-value-key": key});
                        } else if (role === "value" && (typeof value === "number" || typeof value === "boolean")) {
                            $input.attr({"data-mapped-value-type": typeof value, "data-mapped-value-key": key});
                        } else {
                            $input.removeAttr("data-mapped-value-type data-mapped-value-key");
                        }
                        $input.val(valueStr).trigger("input");
                        floatMdcTextFieldLabel($input);
                    });
            }
        });
    }

    // Populate all input fields
    $("form#editServiceWizardForm input[data-param-name], form#editServiceWizardForm textarea[data-param-name]").each(function () {
        const $input = $(this);
        const paramName = $input.data("param-name");

        if (!paramName) {
            return;
        }
        if (paramName === "clientSecrets" && $input.attr("id") === "registeredServiceClientSecrets") {
            return;
        }
        if (paramName === "contacts") {
            return;
        }

        // Skip if this is part of a mapped field (handled separately)
        if ($input.closest("[id$='MapContainer']").length > 0) {
            return;
        }

        const reader = $input.data("reader");
        const setting = getNestedValue(serviceDefinition, paramName);
        const value = typeof reader === "function" ? reader(setting) : setting;

        if (value !== undefined && value !== null) {
            const stringValue = arrayToString(value);
            $input.val(stringValue);

            // Special handling for description field with WYSIWYG editor
            if (paramName === "description") {
                setDescriptionEditorValue(stringValue);
            }

            // Float the label if we have a value
            if (stringValue.length > 0) {
                floatMdcTextFieldLabel($input);
            }
        }
    });

    populateRegisteredServiceContacts(serviceDefinition, floatMdcTextFieldLabel);

    // Populate select fields
    $("form#editServiceWizardForm select[data-param-name]").each(function () {
        const $select = $(this);
        const paramName = $select.data("param-name");

        if (!paramName) {
            return;
        }

        let value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            // Handle objects with @class property
            if (typeof value === "object" && value["@class"]) {
                value = value["@class"];
            }

            setSelectValue($select, value);
        }
    });

    // Populate multi-select fields (TomSelect)
    $("form#editServiceWizardForm select.jqueryui-multiselectmenu[data-param-name]").each(function () {
        const $select = $(this);
        const paramName = $select.data("param-name");

        if (!paramName) {
            return;
        }

        let value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            const arrayValue = arrayToString(value);
            setMultiSelectValue($select, arrayValue);
        }
    });

    // Populate switch buttons (hidden inputs with corresponding buttons)
    $("form#editServiceWizardForm input[type='hidden'][data-param-name]").each(function () {
        const $input = $(this);
        const paramName = $input.data("param-name");
        const inputId = $input.attr("id");

        if (!paramName || !inputId) {
            return;
        }

        const value = getNestedValue(serviceDefinition, paramName);

        if (value !== undefined && value !== null) {
            setSwitchButtonValue(inputId, value);
        }
    });

    setTimeout(() => {
        const mappedFieldContainers = new Set();

        // First, handle attributeReleasePolicy.allowedAttributes specially
        // because it needs to find the right container based on the policy class
        if (serviceDefinition.attributeReleasePolicy && serviceDefinition.attributeReleasePolicy.allowedAttributes) {
            const policyClass = serviceDefinition.attributeReleasePolicy["@class"];
            const policyShortName = getLastWord(policyClass);

            // Find the container with the matching policy class
            $("form#editServiceWizardForm [id$='MapContainer']").each(function () {
                const $container = $(this);
                const $inputs = $container.find("input[data-param-name='attributeReleasePolicy.allowedAttributes']");

                if ($inputs.length > 0 && $container.hasClass(policyShortName)) {
                    const containerKey = `attributeReleasePolicy.allowedAttributes:${$container.attr("id")}`;
                    if (!mappedFieldContainers.has(containerKey)) {
                        mappedFieldContainers.add(containerKey);
                        populateMappedField("attributeReleasePolicy.allowedAttributes",
                            serviceDefinition.attributeReleasePolicy.allowedAttributes, policyClass);
                        return false; // break
                    }
                }
            });
        }

        // Then handle other mapped fields
        $("form#editServiceWizardForm [id$='MapContainer']").each(function () {
            const $container = $(this);

            // Skip disabled policy containers, including when their sidebar section is inactive
            if (isWizardContainerSwitchedOff($container[0])) {
                return;
            }

            const $inputs = $container.find("input[data-param-name]");

            if ($inputs.length > 0) {
                const paramName = $inputs.first().data("param-name");
                // Create a unique key based on paramName and container ID to avoid conflicts
                const containerKey = `${paramName}:${$container.attr("id")}`;
                if (paramName && !mappedFieldContainers.has(containerKey)) {
                    mappedFieldContainers.add(containerKey);
                    const mapData = getNestedValue(serviceDefinition, paramName);
                    if (mapData) {
                        // Pass the attribute release policy class if applicable
                        const policy = paramName.includes(".") ? serviceDefinition[paramName.split(".")[0]] : null;
                        const policyClass = policy !== null && typeof policy === "object" && !Array.isArray(policy)
                            ? policy["@class"] ?? null
                            : null;
                        populateMappedField(paramName, mapData, policyClass);
                    }
                }
            }
        });
    }, 0);

    // Handle special cases for different service types
    const serviceClass = serviceDefinition["@class"];
    if (serviceClass) {
        // OAuth/OIDC specific fields
        if (serviceClass.includes("OAuthRegisteredService") || serviceClass.includes("OidcRegisteredService")) {
            if (typeof populateRegisteredServiceClientSecrets === "function") {
                populateRegisteredServiceClientSecrets(serviceDefinition);
            }
            if (serviceDefinition.supportedGrantTypes) {
                const $grantTypes = $("select#registeredServiceSupportedGrantTypes");
                setMultiSelectValue($grantTypes, arrayToString(serviceDefinition.supportedGrantTypes));
            }
            if (serviceDefinition.supportedResponseTypes) {
                const $responseTypes = $("select#registeredServiceSupportedResponseTypes");
                setMultiSelectValue($responseTypes, arrayToString(serviceDefinition.supportedResponseTypes));
            }
            if (serviceDefinition.scopes) {
                const $scopes = $("select#registeredServiceScopes");
                setMultiSelectValue($scopes, arrayToString(serviceDefinition.scopes));
            }
        }

        // SAML specific fields
        if (serviceClass.includes("SamlRegisteredService")) {
            if (serviceDefinition.attributeNameFormats) {
                populateMappedField("attributeNameFormats", serviceDefinition.attributeNameFormats);
            }
            if (serviceDefinition.attributeFriendlyNames) {
                populateMappedField("attributeFriendlyNames", serviceDefinition.attributeFriendlyNames);
            }
            if (serviceDefinition.attributeValueTypes) {
                populateMappedField("attributeValueTypes", serviceDefinition.attributeValueTypes);
            }
        }
    }

    setTimeout(() => {
        isPopulatingWizard = false;

        // Clean up any missing-required-field classes on fields that have values
        $("form#editServiceWizardForm input[data-param-name]").each(function () {
            const $input = $(this);
            const value = $input.val();
            if (value && value.length > 0) {
                $input.parent().removeClass("missing-required-field");
                if (this.setCustomValidity) {
                    this.setCustomValidity("");
                }
            }
        });

        generateServiceDefinition();
        $wizardDialog.removeClass("is-populating").removeAttr("aria-busy");
        PalantirWizardChains.render(true);
        if (typeof options.done === "function") {
            options.done();
        }
        if (options.focus === false) {
            return;
        }

        // Focus on the first visible input field after dialog is fully ready
        // Use a longer timeout to ensure MDC dialog animation is complete
        setTimeout(() => {
            const $firstInput = $("form#editServiceWizardForm input[data-param-name]:visible").first();
            if ($firstInput.length > 0) {
                $firstInput.trigger("focus");
            }
        }, 300);
    }, 0);
}

/**
 * The contacts the wizard's three contact tabs hold while a definition is generated, by tab, so the details of one
 * tab make one contact whichever tabs before it are empty.
 */
const wizardContactSlots = new WeakMap();

/**
 * The contacts after the third of the definition the wizard was filled from. The wizard has fields for three contacts;
 * the others are kept as they are and added after the wizard's when it generates the definition.
 */
let wizardExtraContacts = [];

/**
 * Fill the contact fields of the wizard from the contacts of a definition, which are a Java list of contact objects:
 * the first three, one per contact tab. The others are kept to be saved as they are.
 *
 * @param serviceDefinition the definition
 * @param floatLabel floats the label of a filled text field
 */
function populateRegisteredServiceContacts(serviceDefinition, floatLabel) {
    const contacts = serviceDefinition?.contacts;
    let list = [];
    if (Array.isArray(contacts)) {
        list = contacts.length === 2 && typeof contacts[0] === "string" && Array.isArray(contacts[1]) ? contacts[1] : contacts;
    }
    wizardExtraContacts = list.slice(3).filter(contact => contact !== null && typeof contact === "object");
    list.slice(0, 3).forEach((contact, position) => {
        ["name", "email", "phone", "department", "type"].forEach(detail => {
            const $input = $(`#registeredServiceContact${capitalize(detail)}${position + 1}`);
            const value = contact?.[detail];
            if ($input.length > 0 && value !== undefined && value !== null && value !== "") {
                $input.val(String(value));
                floatLabel($input);
            }
        });
    });
}

/**
 * Creates the wizard dialog, or hands back the one already created.
 *
 * A jQuery UI dialog rather than an MDC one, matching every other dialog in
 * Palantir. The widget supplies the focus trap, Escape handling, a title bar
 * and role=dialog, which the hand-written MDC markup did not.
 *
 * Called before the Ace editor is attached, because creating the dialog moves
 * its element to the end of the document and Ace does not enjoy being moved
 * after the fact.
 *
 * @returns {{open: function, close: function}} a handle the shared save handler
 *          in palantir-services.js can use the same way as the plain editor
 *          dialog's MDC instance
 */
function createRegisteredServiceWizardDialog() {
    const element = $("#editServiceWizardDialog");
    if (!element.hasClass("ui-dialog-content")) {
        element.removeClass("hide").dialog({
            autoOpen: false,
            modal: true,
            closeOnEscape: true,
            draggable: false,
            resizable: false,
            classes: {"ui-dialog": "ui-corner-all registered-service-wizard-dialog"},
            beforeClose: () => PalantirServiceEditGuard.beforeClose("wizard"),
            close: () => {
                $(window).off("resize.registeredServiceWizard");
                PalantirServiceEditGuard.closed("wizard");
            }
        });
    }
    return {
        open: () => {
            element.dialog("open");
            sizeRegisteredServiceWizardDialog();
            $(window)
                .off("resize.registeredServiceWizard")
                .on("resize.registeredServiceWizard", sizeRegisteredServiceWizardDialog);
        },
        close: () => element.dialog("close")
    };
}

/**
 * Sizes the wizard dialog to the window, leaving a small margin.
 *
 * The editor is a working surface rather than a message, so it takes the whole
 * window. Ace is told to re-measure afterwards, since it cannot detect a
 * container resize on its own.
 */
function sizeRegisteredServiceWizardDialog() {
    const element = $("#editServiceWizardDialog");
    if (!element.hasClass("ui-dialog-content") || !element.dialog("isOpen")) {
        return;
    }
    const margin = 24;
    element.dialog("option", {
        width: Math.max(320, $(window).width() - margin * 2),
        height: Math.max(320, $(window).height() - margin * 2)
    });
    element.dialog("option", "position", {my: "center", at: "center", of: window});
    const editor = ace.edit("wizardServiceEditor");
    if (editor) {
        editor.resize(true);
    }
}

async function openRegisteredServiceWizardDialog(existingService = null, options = {}) {
    await buildRegisteredServiceWizardFields();

    async function openWizardDialog(serviceClass, serviceData = null) {
        wizardExtraContacts = [];
        const isNewService = serviceData === null || options.duplicate === true || options.asNew === true;
        const draftKey = PalantirServiceEditGuard.key("wizard", isNewService ? null : serviceData.id, serviceClass);
        if (!isNewService) {
            PalantirServiceEditGuard.remember(draftKey, serviceData);
        }
        const draft = await PalantirServiceEditGuard.offerDraft(draftKey,
            serviceData === null || isNewService ? null : JSON.stringify(serviceData));
        let draftDefinition = null;
        if (draft !== null) {
            try {
                draftDefinition = JSON.parse(draft);
            } catch (error) {
                PalantirServiceEditGuard.removeDraft(draftKey);
            }
        }

        editServiceWizardDialog = createRegisteredServiceWizardDialog();
        PalantirWizardChains.reset();
        PalantirWizardChains.bind();
        $("#editServiceWizardGeneralContainer").find("input").val("");
        $(".jqueryui-multiselectmenu").each(function () {
            this.tomselect?.clear();
        });

        // Reset the Trumbowyg description editor
        setDescriptionEditorValue("");

        const editor = initializeAceEditor("wizardServiceEditor");
        editor.setReadOnly(true);
        editor.setValue("");
        editor.gotoLine(1);

        const className = getLastWord(serviceClass);
        $("#serviceClassType").text(serviceClass);
        $("#editServiceWizardForm").data("service-class", serviceClass).data("service-class-name", className);

        RegisteredServiceSections.initialize();

        showElements($("#editServiceWizardForm").find(`.class-${className}`));
        hideElements($("#editServiceWizardForm [class*='class-']").not(`.class-${className}`).not(".always-show"));
        placeRegisteredServiceIdField(className);

        RegisteredServiceSections.refresh();

        const editServiceWizardDialogElement = document.getElementById("editServiceWizardDialog");

        $(editServiceWizardDialogElement).attr("newService", isNewService);
        $(editServiceWizardDialogElement).attr("serviceClass", serviceClass);

        $("#editServiceWizardForm input[type=hidden]")
            .each(function () {
                const id = this.id;

                const data = $(`input#${id}`).data("attrs");
                if (data && data.length > 0) {
                    const attributes = Object.fromEntries(
                        data.split(",").map(pair => {
                            const [key, val] = pair.split("=");
                            return [key, val === "true" ? true : val === "false" ? false : val];
                        })
                    );

                    Object.entries(attributes).forEach(([key, value]) => {
                        $(`input#${id}`).data(key, value);
                    });
                }
                $(`button#${id}Button`).off("click.wizardGenerate").on("click.wizardGenerate", function (e) {
                    setTimeout(() => generateServiceDefinition(), 0);
                });
            });

        resetWizardSwitches();
        $("#editServiceWizardForm input").val("");
        if (typeof resetRegisteredServiceClientSecrets === "function") {
            resetRegisteredServiceClientSecrets();
        }
        $("#editServiceWizardForm option").filter(function () {
            const clazz = $(this).data("serviceClass");
            return clazz !== undefined && clazz !== null;
        }).remove();
        resetWizardSelects();

        const dropdown = $("#editServiceWizardForm select.jqueryui-selectmenu");
        try {
            dropdown.selectmenu("refresh");
        } catch (e) {
            dropdown.selectmenu();
        }
        dropdown.selectmenu("option", "appendTo", $(editServiceWizardDialogElement).dialog("widget"));

        const definition = draftDefinition ?? serviceData;
        if (definition !== null) {
            isPopulatingWizard = true;
            setTimeout(() => {
                populateWizardFromServiceDefinition(definition);
            }, 0);
        } else {
            generateServiceDefinition();
        }

        editServiceWizardDialog["open"]();
        PalantirServiceEditGuard.watch("wizard", draftKey, draftDefinition !== null || options.unsaved === true);
        PalantirServiceEditGuard.state.wizard.fromDraft = draftDefinition !== null;

        const savedAdvancedOptionsPref = localStorage.getItem("hideAdvancedOptionsPreference") ?? "true";
        const currentAdvancedOptionsValue = $("#hideAdvancedOptions").val();
        if ((savedAdvancedOptionsPref === "true") !== (currentAdvancedOptionsValue === "true")) {
            $("#hideAdvancedOptionsButton").click();
        }
        hideAdvancedRegisteredServiceOptions();

        // Restore the JSON editor visibility preference from localStorage
        const savedJsonEditorPref = localStorage.getItem("showJsonEditorPreference");
        const currentJsonEditorValue = $("#showJsonEditor").val();
        if (savedJsonEditorPref !== null) {
            // If saved preference differs from current state, toggle it
            if ((savedJsonEditorPref === "true") !== (currentJsonEditorValue === "true")) {
                $("#showJsonEditorButton").click();
            }
        }
        toggleJsonEditorVisibility();

        switch (className) {
        case "CasRegisteredService":
            $("#registeredServiceIdLabel span.mdc-floating-label").text("Service ID");
            break;
        case "SamlRegisteredService":
            $("#registeredServiceIdLabel span.mdc-floating-label").text("Entity ID");
            createSamlRegisteredServiceAttributeReleasePolicy();
            break;
        case "OAuthRegisteredService":
            $("#registeredServiceIdLabel span.mdc-floating-label").text("Redirect URI");
            $(`h3.class-${className}`).each(function () {
                const original = $(this).text();
                const updated = original.replace(/^(?:OAuth \/ OpenID Connect|OpenID Connect) /, "OAuth ");
                $(this).text(updated);
            });
            RegisteredServiceSections.refresh();
            break;
        case "OidcRegisteredService":
            $("#registeredServiceIdLabel span.mdc-floating-label").text("Redirect URI");
            $(`h3.class-${className}`).each(function () {
                const original = $(this).text();
                const updated = original.replace(/^(?:OAuth \/ OpenID Connect|OAuth) /, "OpenID Connect ");
                $(this).text(updated);
            });
            RegisteredServiceSections.refresh();
            createOidcRegisteredServiceFields();
            break;
        }
        RegisteredServiceSections.start();
        if (className === "SamlRegisteredService") {
            showElements($("#fillServiceWizardFromMetadata"));
        } else {
            hideElements($("#fillServiceWizardFromMetadata"));
        }
        const protocol = applicationProtocolOf(serviceClass);
        let title = isNewService ? `New ${protocol.label}` : `Edit ${serviceData.name ?? protocol.label}`;
        if (options.duplicate === true) {
            title = `Duplicate ${options.original ?? protocol.label}`;
        }
        $(editServiceWizardDialogElement).dialog("option", "title", title);
        setTimeout(function () {
            $("#editServiceWizardForm input[data-param-name]:visible:enabled").first().focus();
        }, 200);
    }

    if (existingService !== null) {
        const serviceClass = existingService["@class"];
        await openWizardDialog(serviceClass, existingService);
    } else if (Object.keys(PalantirDashboardConfiguration.supportedServiceTypes()).length === 1) {
        openWizardDialog(Object.keys(PalantirDashboardConfiguration.supportedServiceTypes())[0]);
    } else {
        const sortedServiceTypes = Object.keys(PalantirDashboardConfiguration.supportedServiceTypes())
            .sort()
            .reduce((acc, key) => {
                acc[key] = PalantirDashboardConfiguration.supportedServiceTypes()[key];
                return acc;
            }, {});
        chooseRegisteredServiceType(sortedServiceTypes).then(serviceClass => {
            if (serviceClass) {
                openWizardDialog(serviceClass);
            }
        });
    }
}

/**
 * Ask which type of application to create, as a set of described cards.
 *
 * @param serviceTypes the supported registered service classes, mapped to their friendly names
 * @returns {Promise<string|null>} the chosen class, or null when the operator cancels
 */
async function chooseRegisteredServiceType(serviceTypes) {
    const descriptions = {
        cas: "Web applications that validate service tickets over the CAS protocol.",
        saml: "Service providers that consume SAML2 assertions, configured from their metadata.",
        oidc: "Applications that sign users in with ID tokens, scopes and claims.",
        oauth: "Clients that obtain access tokens for APIs, without an OpenID Connect sign-in.",
        wsfed: "Relying parties that use passive WS-Federation requests."
    };
    const needs = {
        cas: "the service URL pattern",
        saml: "the entity ID and a metadata location",
        oidc: "a redirect URI, client ID and secret",
        oauth: "a redirect URI, client ID and grant types",
        wsfed: "the realm and reply URL"
    };
    const cards = Object.entries(serviceTypes).map(([serviceClass, friendlyName]) => {
        const protocol = applicationProtocolOf(serviceClass);
        return `
            <button type="button" class="palantir-type-card" role="radio" aria-checked="false"
                    data-service-class="${escapePalantirMarkup(serviceClass)}">
                <span class="palantir-type-card-head">
                    <span class="palantir-protocol-badge is-${protocol.key}" aria-hidden="true">${escapePalantirMarkup(protocol.short)}</span>
                    <span class="palantir-type-card-name">
                        <span>${escapePalantirMarkup(friendlyName || protocol.label)}</span>
                        ${palantirCode(serviceClass.split(".").pop())}
                    </span>
                </span>
                <span class="palantir-type-card-description">${escapePalantirMarkup(descriptions[protocol.key] ?? "Applications of this registered service type.")}</span>
                ${needs[protocol.key] ? `<span class="palantir-type-card-needs">You will need ${needs[protocol.key]}.</span>` : ""}
            </button>`;
    }).join("");
    let chosen = null;
    const result = await Swal.fire({
        title: "What type of application do you want to add?",
        html: `
            <p class="palantir-type-picker-intro">The type decides which protocol settings the wizard shows.</p>
            <div class="palantir-type-picker" role="radiogroup" aria-label="Application type">${cards}</div>`,
        width: 920,
        showConfirmButton: false,
        showCancelButton: true,
        customClass: {popup: "palantir-type-picker-popup"},
        didOpen: popup => {
            const choose = card => {
                chosen = card.dataset.serviceClass;
                popup.querySelectorAll(".palantir-type-card").forEach(entry => {
                    entry.setAttribute("aria-checked", String(entry === card));
                });
                Swal.resetValidationMessage();
            };
            const cards = [...popup.querySelectorAll(".palantir-type-card")];
            cards.forEach((card, index) => {
                card.addEventListener("click", () => {
                    choose(card);
                    Swal.clickConfirm();
                });
                card.addEventListener("keydown", event => {
                    const step = {ArrowRight: 1, ArrowDown: 1, ArrowLeft: -1, ArrowUp: -1}[event.key];
                    if (step) {
                        event.preventDefault();
                        const next = cards[(index + step + cards.length) % cards.length];
                        choose(next);
                        next.focus();
                    }
                });
            });
            cards[0]?.focus();
        },
        preConfirm: () => {
            if (!chosen) {
                Swal.showValidationMessage("Choose the type of application to continue.");
                return false;
            }
            return chosen;
        }
    });
    return result.isConfirmed ? result.value : null;
}

/**
 * Suggestions for the wizard's text fields whose values usually come from a standard or from what CAS knows, such as
 * SAML2 NameID formats, JOSE algorithms, OAuth token types or the attributes CAS can resolve. A suggestion only helps:
 * the field still takes any value. Fields that hold a comma-separated list are completed one entry at a time.
 */
class PalantirWizardSuggestions {
    static choices(...entries) {
        return entries.map(([value, description = ""]) => ({value, description}));
    }

    static DURATIONS = PalantirWizardSuggestions.choices(
        ["PT30S", "30 seconds"], ["PT1M", "1 minute"], ["PT5M", "5 minutes"], ["PT15M", "15 minutes"],
        ["PT30M", "30 minutes"], ["PT1H", "1 hour"], ["PT8H", "8 hours"], ["P1D", "1 day"], ["P7D", "7 days"],
        ["P30D", "30 days"]);

    static TIME_UNITS = PalantirWizardSuggestions.choices(
        ["SECONDS"], ["MINUTES"], ["HOURS"], ["DAYS"], ["MILLISECONDS"]);

    static HTTP_METHODS = PalantirWizardSuggestions.choices(
        ["GET"], ["POST"], ["PUT"], ["PATCH"], ["DELETE"]);

    static JWS_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["RS256", "RSASSA-PKCS1-v1_5 using SHA-256, the OpenID Connect default"],
        ["RS384", "RSASSA-PKCS1-v1_5 using SHA-384"], ["RS512", "RSASSA-PKCS1-v1_5 using SHA-512"],
        ["PS256", "RSASSA-PSS using SHA-256"], ["PS384", "RSASSA-PSS using SHA-384"], ["PS512", "RSASSA-PSS using SHA-512"],
        ["ES256", "ECDSA using P-256 and SHA-256"], ["ES384", "ECDSA using P-384 and SHA-384"],
        ["ES512", "ECDSA using P-521 and SHA-512"], ["EdDSA", "Edwards-curve signatures, such as Ed25519"],
        ["HS256", "HMAC using SHA-256, keyed with the client secret"], ["HS384", "HMAC using SHA-384"],
        ["HS512", "HMAC using SHA-512"]);

    static JWE_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["RSA-OAEP-256", "RSAES OAEP using SHA-256"], ["RSA-OAEP", "RSAES OAEP using SHA-1"],
        ["ECDH-ES", "Elliptic curve Diffie-Hellman, direct key agreement"],
        ["ECDH-ES+A128KW", "ECDH-ES with AES key wrap, 128 bits"], ["ECDH-ES+A192KW", "ECDH-ES with AES key wrap, 192 bits"],
        ["ECDH-ES+A256KW", "ECDH-ES with AES key wrap, 256 bits"], ["A128KW", "AES key wrap, 128 bits"],
        ["A192KW", "AES key wrap, 192 bits"], ["A256KW", "AES key wrap, 256 bits"],
        ["A128GCMKW", "AES GCM key wrap, 128 bits"], ["A256GCMKW", "AES GCM key wrap, 256 bits"],
        ["dir", "A shared symmetric key used directly"]);

    static JWE_ENCODINGS = PalantirWizardSuggestions.choices(
        ["A128CBC-HS256", "AES-128-CBC with HMAC SHA-256"], ["A192CBC-HS384", "AES-192-CBC with HMAC SHA-384"],
        ["A256CBC-HS512", "AES-256-CBC with HMAC SHA-512"], ["A128GCM", "AES GCM, 128 bits"],
        ["A192GCM", "AES GCM, 192 bits"], ["A256GCM", "AES GCM, 256 bits"]);

    static TOKEN_TYPES = PalantirWizardSuggestions.choices(
        ["urn:ietf:params:oauth:token-type:access_token", "OAuth access token"],
        ["urn:ietf:params:oauth:token-type:refresh_token", "OAuth refresh token"],
        ["urn:ietf:params:oauth:token-type:id_token", "OpenID Connect ID token"],
        ["urn:ietf:params:oauth:token-type:jwt", "A JWT"],
        ["urn:ietf:params:oauth:token-type:saml2", "A SAML 2.0 assertion"],
        ["urn:ietf:params:oauth:token-type:saml1", "A SAML 1.1 assertion"]);

    static XML_SIGNATURE_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "RSA with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "RSA with SHA-384"],
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "RSA with SHA-512"],
        ["http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1", "RSA-PSS with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", "ECDSA with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", "ECDSA with SHA-384"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", "ECDSA with SHA-512"],
        ["http://www.w3.org/2000/09/xmldsig#rsa-sha1", "RSA with SHA-1, for older service providers"]);

    static XML_DIGEST_METHODS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384"],
        ["http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512"],
        ["http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1, for older service providers"]);

    static XML_CANONICALIZATIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/10/xml-exc-c14n#", "Exclusive canonicalization"],
        ["http://www.w3.org/2001/10/xml-exc-c14n#WithComments", "Exclusive canonicalization, keeping comments"],
        ["http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "Canonical XML 1.0"],
        ["http://www.w3.org/2006/12/xml-c14n11", "Canonical XML 1.1"]);

    static XML_DATA_ENCRYPTIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2009/xmlenc11#aes128-gcm", "AES-128 GCM"],
        ["http://www.w3.org/2009/xmlenc11#aes192-gcm", "AES-192 GCM"],
        ["http://www.w3.org/2009/xmlenc11#aes256-gcm", "AES-256 GCM"],
        ["http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES-128 CBC, for older service providers"],
        ["http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES-256 CBC, for older service providers"]);

    static XML_KEY_ENCRYPTIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2009/xmlenc11#rsa-oaep", "RSA-OAEP"],
        ["http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", "RSA-OAEP with MGF1 and SHA-1"],
        ["http://www.w3.org/2001/04/xmlenc#kw-aes128", "AES-128 key wrap"],
        ["http://www.w3.org/2001/04/xmlenc#kw-aes256", "AES-256 key wrap"],
        ["http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA PKCS#1 v1.5, for older service providers"]);

    static NAME_ID_FORMATS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:persistent", "A stable, opaque identifier per service provider"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:transient", "A one-time identifier"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", "An email address"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "Any identifier the IdP chooses"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName", "An X.509 subject name"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName", "A Windows domain qualified name"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos", "A Kerberos principal name"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:entity", "An entity id"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted", "An encrypted identifier"]);

    static AUTHENTICATION_CONTEXTS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", "A password over a protected session"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Password", "A password"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:X509", "An X.509 certificate"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient", "A TLS client certificate"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos", "Kerberos"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard", "A smart card"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract", "A mobile second factor"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken", "A time-based one-time password"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified", "Any authentication"],
        ["https://refeds.org/profile/mfa", "REFEDS multifactor authentication profile"],
        ["https://refeds.org/profile/sfa", "REFEDS single-factor authentication profile"]);

    static SAML_BINDINGS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", "HTTP POST"],
        ["urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect", "HTTP Redirect"],
        ["urn:oasis:names:tc:SAML:2.0:bindings:SOAP", "SOAP back channel"]);

    static ATTRIBUTE_NAME_FORMATS = PalantirWizardSuggestions.choices(
        ["uri", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"],
        ["basic", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic"],
        ["unspecified", "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"]);

    static ENTITY_CATEGORIES = PalantirWizardSuggestions.choices(
        ["http://refeds.org/category/research-and-scholarship", "REFEDS Research and Scholarship"],
        ["https://refeds.org/category/code-of-conduct/v2", "REFEDS Data Protection Code of Conduct"],
        ["http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "GÉANT Data Protection Code of Conduct"],
        ["https://refeds.org/category/personalized", "REFEDS Personalized Access"],
        ["https://refeds.org/category/pseudonymous", "REFEDS Pseudonymous Access"],
        ["https://refeds.org/category/anonymous", "REFEDS Anonymous Access"],
        ["https://refeds.org/sirtfi", "Sirtfi security incident response"]);

    static ENTITY_ATTRIBUTE_NAMES = PalantirWizardSuggestions.choices(
        ["http://macedir.org/entity-category", "Entity categories the service provider belongs to"],
        ["http://macedir.org/entity-category-support", "Entity categories the service provider supports"],
        ["urn:oasis:names:tc:SAML:attribute:assurance-certification", "Assurance certifications, such as Sirtfi"],
        ["urn:oasis:names:tc:SAML:profiles:subject-id:req", "The subject identifier the service provider requires"]);

    static ENTITY_ATTRIBUTE_VALUES = [
        ...PalantirWizardSuggestions.ENTITY_CATEGORIES,
        ...PalantirWizardSuggestions.choices(
            ["http://id.incommon.org/category/research-and-scholarship", "InCommon Research and Scholarship"],
            ["http://refeds.org/category/hide-from-discovery", "Hidden from discovery services"],
            ["https://refeds.org/sirtfi2", "Sirtfi version 2"],
            ["subject-id", "Requires a subject identifier"],
            ["pairwise-id", "Requires a pairwise identifier"],
            ["any", "Accepts a subject or pairwise identifier"],
            ["none", "Requires no subject identifier"])
    ];

    static LOCALES = PalantirWizardSuggestions.choices(
        ["en", "English"], ["ar", "Arabic"], ["ca", "Catalan"], ["cs", "Czech"], ["de", "German"], ["es", "Spanish"],
        ["fa", "Persian"], ["fr", "French"], ["hr", "Croatian"], ["it", "Italian"], ["ja", "Japanese"],
        ["mk", "Macedonian"], ["nl", "Dutch"], ["pl", "Polish"], ["pt_BR", "Portuguese (Brazil)"],
        ["pt_PT", "Portuguese (Portugal)"], ["ru", "Russian"], ["sk", "Slovak"], ["sl", "Slovenian"],
        ["sv", "Swedish"], ["tr", "Turkish"], ["uk", "Ukrainian"], ["ur", "Urdu"], ["vi", "Vietnamese"],
        ["zh_CN", "Chinese (Simplified)"], ["zh_TW", "Chinese (Traditional)"]);

    static SECONDS = PalantirWizardSuggestions.choices(
        ["300", "5 minutes"], ["1800", "30 minutes"], ["3600", "1 hour"], ["28800", "8 hours"], ["86400", "1 day"],
        ["604800", "7 days"]);

    static HEADERS = PalantirWizardSuggestions.choices(
        ["Authorization", "Credentials, such as a bearer token or basic authentication"], ["Accept", "The media types accepted"],
        ["Accept-Language", "The languages accepted"], ["Content-Type", "The media type of the body"],
        ["Cache-Control", "Caching directives"], ["User-Agent", "The client software"],
        ["X-Request-ID", "An identifier to trace the request"], ["X-Forwarded-For", "The client address behind a proxy"]);

    static ONE_USE = PalantirWizardSuggestions.choices(["1", "Used once, as the protocol expects"]);

    static OIDC_CLAIMS = PalantirWizardSuggestions.choices(
        ["sub", "Subject"], ["name", "Full name"], ["given_name", "Given name"], ["family_name", "Family name"],
        ["middle_name", "Middle name"], ["nickname", "Casual name"], ["preferred_username", "Preferred username"],
        ["profile", "Profile page"], ["picture", "Profile picture"], ["website", "Web page or blog"],
        ["gender", "Gender"], ["birthdate", "Birthday"], ["zoneinfo", "Time zone"], ["locale", "Locale"],
        ["updated_at", "Last profile update"], ["email", "Email address"], ["email_verified", "Whether the email is verified"],
        ["address", "Postal address"], ["phone_number", "Phone number"],
        ["phone_number_verified", "Whether the phone number is verified"]);

    /**
     * The client ids of the OAuth and OpenID Connect applications CAS has, as audiences a token may be exchanged for.
     *
     * @returns {object[]} the client ids, described by the application's name
     */
    static clientIds() {
        return [...registeredServicesById.values()]
            .filter(service => service?.clientId)
            .map(service => ({value: String(service.clientId), description: String(service.name ?? "")}));
    }

    static attributes() {
        return CasDiscoveryProfile.availableAttributes().map(name => ({value: name, description: "An attribute CAS can resolve"}));
    }

    static timeZones() {
        const zones = typeof Intl.supportedValuesOf === "function" ? Intl.supportedValuesOf("timeZone") : [];
        return [{value: "UTC", description: "Coordinated Universal Time"}, ...zones.map(zone => ({value: zone, description: ""}))];
    }

    static authenticationHandlers() {
        return CasDiscoveryProfile.availableAuthenticationHandlers().map(name => ({value: name, description: "An authentication handler of this server"}));
    }

    static identityProviderNames = null;

    /**
     * The external identity providers CAS delegates to, read once from the delegated clients endpoint.
     *
     * @returns {object[]|Promise<object[]>} the providers, or a promise of them while they are read
     */
    static identityProviders() {
        if (Array.isArray(PalantirWizardSuggestions.identityProviderNames)) {
            return PalantirWizardSuggestions.identityProviderNames;
        }
        if (!CasActuatorEndpoints.delegatedClients()) {
            return [];
        }
        return Promise.resolve($.get(CasActuatorEndpoints.delegatedClients()))
            .then(response => {
                const names = response && typeof response === "object" && !Array.isArray(response) ? Object.keys(response).sort() : [];
                PalantirWizardSuggestions.identityProviderNames = names.map(name => ({value: name, description: "An external identity provider of this server"}));
                return PalantirWizardSuggestions.identityProviderNames;
            });
    }

    static multifactorProviders() {
        const providers = Object.entries(CasDiscoveryProfile.multifactorAuthenticationProviders())
            .map(([id, name]) => ({value: id, description: String(name)}));
        return providers.length > 0 ? providers : PalantirWizardSuggestions.choices(
            ["mfa-duo", "Duo Security"], ["mfa-gauth", "Google Authenticator"], ["mfa-webauthn", "WebAuthn"],
            ["mfa-yubikey", "YubiKey"], ["mfa-simple", "One-time codes by email or text message"],
            ["mfa-radius", "RADIUS"], ["mfa-inwebo", "inWebo"], ["mfa-twilio", "Twilio"]);
    }

    /**
     * The suggestions of each field, by the dotted path it writes to, with <code>:key</code> or <code>:value</code>
     * for the two sides of a field that edits a map. A list field is completed one comma-separated entry at a time.
     */
    static FIELDS = {
        "idTokenSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "userInfoSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "jwtAccessTokenSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "introspectionSignedResponseAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "backchannelAuthenticationRequestSigningAlg": {
            source: () => PalantirWizardSuggestions.JWS_ALGORITHMS.filter(choice => !choice.value.startsWith("HS"))
        },
        "idTokenEncryptionAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "userInfoEncryptedResponseAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "introspectionEncryptedResponseAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "idTokenEncryptionEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "userInfoEncryptedResponseEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "introspectionEncryptedResponseEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "tokenEndpointAuthenticationMethod": {source: () => PalantirWizardSuggestions.choices(
            ["client_secret_basic", "Client secret in the HTTP Basic header"],
            ["client_secret_post", "Client secret in the request body"],
            ["client_secret_jwt", "A JWT signed with the client secret"],
            ["private_key_jwt", "A JWT signed with the client's private key"],
            ["tls_client_auth", "A TLS client certificate issued by a trusted authority"],
            ["self_signed_tls_client_auth", "A self-signed TLS client certificate"],
            ["none", "A public client that does not authenticate"])},
        "applicationType": {source: () => PalantirWizardSuggestions.choices(["web", "A web application"], ["native", "A native or mobile application"])},
        "subjectType": {source: () => PalantirWizardSuggestions.choices(
            ["public", "The same subject for every client"], ["pairwise", "A different subject for each sector"])},
        "jwksCacheDuration": {source: () => PalantirWizardSuggestions.DURATIONS},
        "idTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "verifiableCredentialsPolicy.credentialSigningAlgValuesSupported": {
            list: true,
            source: () => PalantirWizardSuggestions.JWS_ALGORITHMS.filter(choice => !choice.value.startsWith("HS"))
        },
        "tokenExchangePolicy.allowedTokenTypes": {list: true, source: () => PalantirWizardSuggestions.TOKEN_TYPES},
        "tokenExchangePolicy.allowedActorTokenTypes": {list: true, source: () => PalantirWizardSuggestions.TOKEN_TYPES},
        "accessTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "accessTokenExpirationPolicy.maxTimeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "refreshTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "deviceTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "codeExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "serviceTicketExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "serviceTicketExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "proxyTicketExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "proxyTicketExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "proxyGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds": {source: () => PalantirWizardSuggestions.SECONDS},
        "ticketGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds": {source: () => PalantirWizardSuggestions.SECONDS},
        "proxyPolicy.pattern": {source: () => PalantirWizardSuggestions.choices(
            ["^https://.+", "Any HTTPS proxy callback"], ["^https://([a-z0-9-]+\\.)*example\\.org/.*", "HTTPS callbacks on one domain"])},
        "proxyPolicy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "codeExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "tokenExchangePolicy.allowedAudience": {list: true, source: () => PalantirWizardSuggestions.clientIds()},
        "tokenExchangePolicy.requiredActorTokenAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "audience": {list: true, source: () => PalantirWizardSuggestions.clientIds()},
        "idTokenIssuer": {source: () => PalantirWizardSuggestions.choices(
            [`${PalantirDashboardConfiguration.casServerPrefix() ?? ""}/oidc`, "The issuer CAS uses by default"])},
        "realm": {source: () => PalantirWizardSuggestions.choices(["urn:org:apereo:cas:ws:idp:realm-CAS", "The realm CAS uses by default"])},
        "accessStrategy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "attributeReleasePolicy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "accessStrategy.rolesAttribute": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.emailAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.firstNameAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.lastNameAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.surrogateRequiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.claimMappings:key": {source: () => PalantirWizardSuggestions.OIDC_CLAIMS},
        "attributeReleasePolicy.claimMappings:value": {source: () => PalantirWizardSuggestions.attributes()},

        "requiredNameIdFormat": {source: () => PalantirWizardSuggestions.NAME_ID_FORMATS},
        "requiredAuthenticationContextClass": {source: () => PalantirWizardSuggestions.AUTHENTICATION_CONTEXTS},
        "logoutResponseBinding": {source: () => PalantirWizardSuggestions.SAML_BINDINGS},
        "signingSignatureAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureBlackListedAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureWhiteListedAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureReferenceDigestMethods": {list: true, source: () => PalantirWizardSuggestions.XML_DIGEST_METHODS},
        "signingSignatureCanonicalizationAlgorithm": {source: () => PalantirWizardSuggestions.XML_CANONICALIZATIONS},
        "encryptionDataAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS},
        "encryptionKeyAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS},
        "encryptionBlackListedAlgorithms": {list: true, source: () => [...PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS, ...PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS]},
        "encryptionWhiteListedAlgorithms": {list: true, source: () => [...PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS, ...PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS]},
        "whiteListBlackListPrecedence": {source: () => PalantirWizardSuggestions.choices(
            ["INCLUDE", "The allowed algorithms win over the blocked ones"], ["EXCLUDE", "The blocked algorithms win over the allowed ones"])},
        "signingCredentialType": {source: () => PalantirWizardSuggestions.choices(
            ["X509", "Sign with an X.509 certificate"], ["BASIC", "Sign with a bare key pair"])},
        "signingKeyAlgorithm": {source: () => PalantirWizardSuggestions.choices(["RSA"], ["EC", "Elliptic curve"])},
        "metadataCriteriaDirection": {source: () => PalantirWizardSuggestions.choices(
            ["INCLUDE", "Keep the entities that match"], ["EXCLUDE", "Remove the entities that match"])},
        "metadataCriteriaRoles": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["SPSSODescriptor", "Service provider role"], ["IDPSSODescriptor", "Identity provider role"])},
        "metadataExpirationDuration": {source: () => PalantirWizardSuggestions.DURATIONS},
        "skewAllowance": {source: () => PalantirWizardSuggestions.DURATIONS.slice(0, 5)},
        "encryptableAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeNameFormats:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeNameFormats:value": {source: () => PalantirWizardSuggestions.ATTRIBUTE_NAME_FORMATS},
        "attributeValueTypes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeValueTypes:value": {source: () => PalantirWizardSuggestions.choices(
            ["XSString", "Text"], ["XSURI", "A URI"], ["XSBoolean", "true or false"], ["XSInteger", "A whole number"],
            ["XSDateTime", "A date and time"], ["XSBase64Binary", "Base64 binary data"],
            ["XSObject", "Values released as they are"], ["NameIDType", "A NameID"])},
        "attributeFriendlyNames:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.entityAttribute": {source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_NAMES},
        "attributeReleasePolicy.entityAttributeValues": {list: true, source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_VALUES},
        "metadataCriteriaEntityAttributes:key": {source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_NAMES},
        "metadataCriteriaEntityAttributes:value": {list: true, source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_VALUES},
        "attributeReleasePolicy.entityAttributeFormat": {source: () => PalantirWizardSuggestions.choices(
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "URI"],
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "Basic"],
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", "Unspecified"])},

        "protocol": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/ws-sx/ws-trust/200512", "WS-Trust 1.3"],
            ["http://schemas.xmlsoap.org/ws/2005/02/trust", "WS-Trust, February 2005"])},
        "tokenType": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0", "SAML 2.0 token"],
            ["http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1", "SAML 1.1 token"])},
        "namespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "WS-Trust 1.3"])},
        "addressingNamespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://www.w3.org/2005/08/addressing", "WS-Addressing 1.0"])},
        "policyNamespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://www.w3.org/ns/ws-policy", "WS-Policy 1.5"], ["http://schemas.xmlsoap.org/ws/2004/09/policy", "WS-Policy 1.2"])},
        "wsdlService": {source: () => PalantirWizardSuggestions.choices(["SecurityTokenService", "The CAS security token service"])},
        "wsdlEndpoint": {source: () => PalantirWizardSuggestions.choices(["TransportUT_Port", "The CAS security token service endpoint"])},

        "locale": {source: () => PalantirWizardSuggestions.LOCALES},
        "supportedProtocols": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["CAS10", "CAS protocol 1.0"], ["CAS20", "CAS protocol 2.0"], ["CAS30", "CAS protocol 3.0"], ["SAML1", "SAML 1.1"])},
        "accessStrategy.zoneId": {source: () => PalantirWizardSuggestions.timeZones()},
        "accessStrategy.method": {source: () => PalantirWizardSuggestions.HTTP_METHODS},
        "accessStrategy.acceptableResponseCodes": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["200", "OK"], ["201", "Created"], ["202", "Accepted"], ["204", "No content"])},
        "attributeReleasePolicy.method": {source: () => PalantirWizardSuggestions.HTTP_METHODS},
        "attributeReleasePolicy.allowedAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.allowedAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.attribute": {source: () => PalantirWizardSuggestions.choices(
            ["eduPersonTargetedID", "The attribute name eduPerson defines"],
            ["urn:oid:1.3.6.1.4.1.5923.1.1.1.10", "The attribute's OID, as SAML2 service providers expect it"])},
        "attributeReleasePolicy.consentPolicy.includeOnlyAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.activationCriteria.operator": {source: () => PalantirWizardSuggestions.choices(
            ["AND", "Every required attribute must match"], ["OR", "Any required attribute may match"])},
        "attributeReleasePolicy.activationCriteria.requiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.principalAttributesRepository.timeUnit": {source: () => PalantirWizardSuggestions.TIME_UNITS},
        "singleSignOnParticipationPolicy.timeUnit": {source: () => PalantirWizardSuggestions.TIME_UNITS},
        "singleSignOnParticipationPolicy.attributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "multifactorPolicy.multifactorAuthenticationProviders": {list: true, source: () => PalantirWizardSuggestions.multifactorProviders()},
        "multifactorPolicy.multifactorProviderSelectionMappings:key": {source: () => PalantirWizardSuggestions.multifactorProviders()},
        "authenticationPolicy.requiredAuthenticationHandlers": {list: true, source: () => PalantirWizardSuggestions.authenticationHandlers()},
        "authenticationPolicy.excludedAuthenticationHandlers": {list: true, source: () => PalantirWizardSuggestions.authenticationHandlers()},
        "accessStrategy.delegatedAuthenticationPolicy.allowedProviders": {list: true, source: () => PalantirWizardSuggestions.identityProviders()},
        "accessStrategy.activationCriteria.requiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.consentPolicy.excludedAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "webflowInterruptPolicy.attributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "publicKey.algorithm": {source: () => PalantirWizardSuggestions.choices(["RSA", "The algorithm CAS encrypts with by default"])}
    };

    /**
     * The suggestions of a field, if it has any.
     *
     * @param input the field
     * @returns {object|null} the field's suggestions
     */
    static rule(input) {
        const path = String($(input).data("param-name") ?? "");
        const role = input.dataset.mappedFieldRole;
        const key = role === "key" || role === "value" ? `${path}:${role}` : path;
        return PalantirWizardSuggestions.FIELDS[key] ?? null;
    }

    static matches(rule, term) {
        return PalantirWizardSuggestions.filter(rule.source(), term);
    }

    static filter(choices, term) {
        const wanted = term.trim().toLowerCase();
        return (Array.isArray(choices) ? choices : [])
            .filter(choice => wanted === "" || choice.value.toLowerCase().includes(wanted) || choice.description.toLowerCase().includes(wanted))
            .slice(0, 50)
            .map(choice => ({label: choice.value, value: choice.value, description: choice.description}));
    }

    /**
     * Offer suggestions on every field of the wizard that has them, including fields added later, such as another row
     * of a map, as each field gets focus.
     *
     * @param form the wizard's form
     */
    static attach(form) {
        if (!form || typeof $.fn.autocomplete !== "function") {
            return;
        }
        $(form).off("focusin.palantirSuggestions").on("focusin.palantirSuggestions", "input[type=text][data-param-name]", event => {
            const input = event.currentTarget;
            const rule = PalantirWizardSuggestions.rule(input);
            if (rule === null) {
                return;
            }
            if (!$(input).autocomplete("instance")) {
                PalantirWizardSuggestions.enable(input, rule);
            }
            $(input).autocomplete("search", PalantirWizardSuggestions.term(input, rule));
        });
    }

    static term(input, rule) {
        const value = String(input.value ?? "");
        return rule.list ? value.split(",").pop().trimStart() : value;
    }

    static enable(input, rule) {
        $(input).attr("autocomplete", "off").autocomplete({
            delay: 0,
            minLength: 0,
            source: (request, respond) => {
                const choices = rule.source();
                const term = PalantirWizardSuggestions.term(input, rule);
                if (typeof choices?.then === "function") {
                    choices.then(list => respond(PalantirWizardSuggestions.filter(list, term)), () => respond([]));
                } else {
                    respond(PalantirWizardSuggestions.filter(choices, term));
                }
            },
            focus: () => false,
            open: () => attachPalantirTextFields($(input).closest(".mdc-text-field")),
            select: (event, ui) => {
                if (rule.list) {
                    const entries = String(input.value ?? "").split(",").map(entry => entry.trim());
                    entries[entries.length - 1] = ui.item.value;
                    input.value = entries.filter(Boolean).join(",");
                } else {
                    input.value = ui.item.value;
                }
                $(input).trigger("input").trigger("change");
                attachPalantirTextFields($(input).closest(".mdc-text-field"));
                return false;
            }
        });
        $(input).autocomplete("instance")._renderItem = (list, item) => $("<li>")
            .addClass("palantir-suggestion")
            .append($("<div>")
                .append($("<code>").text(item.value))
                .append(item.description ? $("<span>").text(item.description) : ""))
            .appendTo(list);
    }
}
