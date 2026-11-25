/* The wizard's sections for attribute release: policies, consent, activation, attribute repositories and value filters. */

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
