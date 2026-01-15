let editServiceWizardDialog = undefined;

function validateServiceIdPattern(button) {
    $("#validateServiceIdDialog").dialog({
        title: "Validate Pattern",
        modal: true,
        width: 600,
        closeOnEscape: false,
        buttons: {
            "Use Pattern": function () {
                $("#registeredServiceId").val($("#serviceIdPattern").val()).focus();
                $(this).dialog("close");
            },
            Cancel: function () {
                $(this).dialog("close");
            }
        },
        close: function () {
            $(this).dialog("destroy");
        }
    });
}

function handleAttributeReleasePolicyChange(select) {
    let type = getLastWord($(select).val());
    showElements($(`#editServiceWizardMenuItemAttributeReleasePolicy .${type}`));
    hideElements($("#editServiceWizardMenuItemAttributeReleasePolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .not(".always-show"));
    hideElements($("#editServiceWizardMenuItemAttributeReleasePolicy [id$='SelectContainer']")
        .not(`.${type}`)
        .not(".always-show"));
    hideElements($("#editServiceWizardMenuItemAttributeReleasePolicy [id$='MapContainer']")
        .not(`.${type}`));
    hideElements($("#editServiceWizardMenuItemAttributeReleasePolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .not(".always-show"));

    $("#editServiceWizardMenuItemAttributeReleasePolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePolicy [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePolicy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
    $("#editServiceWizardMenuItemAttributeReleasePolicy .jqueryui-multiselectmenu").each(function () {
        this.tomselect?.clear();
    });
}

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
                scriptFactoryAvailable
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
        .done(async () => {
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
            })
            
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
                cssClasses: "hide ReturnAllowedAttributeReleasePolicy",
                containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
                labelTitle: "Allowed Attributes:",
                paramName: "attributeReleasePolicy.allowedAttributes",
                title: "Define the attribute names for release, separated by comma.",
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
        containerField: "attributeReleasePolicy.allowedAttributes"
    });

    createMappedInputField({
        cssClasses: "hide PatternMatchingAttributeReleasePolicy",
        header: "Allowed Attributes",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePatternMatchingAllowedAttrs",
        keyLabel: "Attribute",
        valueField: "registeredServiceAttrReleasePolicyPatternMatchingAttrValue",
        valueLabel: "Value Pattern",
        containerField: "attributeReleasePolicy.allowedAttributes",
        valueFieldRenderer: function ($inputKey, $inputValue) {
            return {
                "@class": "org.apereo.cas.services.PatternMatchingAttributeReleasePolicy$Rule",
                "pattern": $inputValue.val().trim()
            };
        }
    });


    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide GroovyScriptAttributeReleasePolicy",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleaseGroovyScript",
            paramName: "attributeReleasePolicy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
            title: "Define the Groovy script location, inline or external, to determine attribute release."
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
        unwrapSingleElement: true
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

    createMappedInputField({
        cssClasses: "hide ReturnEnvironmentAttributeReleasePolicy",
        header: "Environment Variables",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicyEnvVarAttrName",
        keyLabel: "Environment Variable Name",
        valueField: "registeredServiceAttrReleasePolicyEnvVarAttrValue",
        valueLabel: "Attribute Name",
        containerField: "attributeReleasePolicy.environmentVariables",
        multipleValues: false
    });

    createMappedInputField({
        cssClasses: "hide ReturnEnvironmentAttributeReleasePolicy",
        header: "System Property",
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        keyField: "registeredServiceAttrReleasePolicySysPropAttrName",
        keyLabel: "System Property",
        valueField: "registeredServiceAttrReleasePolicySysPropAttrValue",
        valueLabel: "Attribute Name",
        containerField: "attributeReleasePolicy.systemProperties",
        multipleValues: false
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

    createInputField({
        cssClasses: "hide GroovySamlRegisteredServiceAttributeReleasePolicy",
        labelTitle: "Groovy Script",
        name: "registeredServiceAttrReleasePolicyGroovySamlScript",
        paramName: "attributeReleasePolicy.groovyScript",
        required: false,
        containerId: "editServiceWizardMenuItemAttributeReleasePolicy",
        title: "Define the Groovy script location to determine attribute release."
    });


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
            {value: "", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specify whether consent is enabled for attribute release."
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
    $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleaseValueConsentPolicy [id$='SelectContainer'] input")
        .val("");
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
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
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
                scriptFactoryAvailable
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

    if (scriptFactoryAvailable) {
        createInputField({
            paramType: "org.apereo.cas.services.GroovyRegisteredServiceAttributeReleaseActivationCriteria",
            cssClasses: "hide GroovyRegisteredServiceAttributeReleaseActivationCriteria",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleasePolicyAttributeActivationCriteriaGroovyScript",
            paramName: "attributeReleasePolicy.activationCriteria.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleasePolicyActivationCriteria",
            title: "Specifies the Groovy script location, inline or external, to determine attribute release activation criteria."
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
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleasePrincipalAttributesRepository [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
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
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAttributeReleaseValueFilters [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
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
                scriptFactoryAvailable
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

    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide RegisteredServiceScriptedAttributeFilter",
            labelTitle: "Groovy Script",
            name: "registeredServiceAttrReleasePolicyAttributeValueFilterScript",
            paramName: "attributeReleasePolicy.attributeFilter.script",
            required: false,
            containerId: "editServiceWizardMenuItemAttributeReleaseValueFilters",
            title: "Specifies the Groovy script location, inline or external, to filter attribute values."
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
        containerField: "attributeReleasePolicy.attributeFilter.patterns",
        multipleValues: false
    });

}

function createRegisteredServiceMultifactorPolicy() {
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

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        cssClasses: "advanced-option",
        labelTitle: "Principal Attribute Name Trigger",
        name: "registeredServiceMfaPrincipalAttributeNameTrigger",
        paramName: "multifactorPolicy.principalAttributeNameTrigger",
        required: false,
        containerId: "editServiceWizardMenuItemMfaPolicy",
        title: "Specifies the principal attribute name that triggers multifactor authentication."
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

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
        cssClasses: "advanced-option",
        labelTitle: "Principal Attribute Name Bypass",
        name: "registeredServiceMfaBypassPrincipalAttributeName",
        paramName: "multifactorPolicy.bypassPrincipalAttributeName",
        required: false,
        containerId: "editServiceWizardMenuItemMfaPolicy",
        title: "Specifies the principal attribute name that bypasses multifactor authentication."
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

    if (scriptFactoryAvailable) {
        createInputField({
            paramType: "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
            cssClasses: "advanced-option",
            labelTitle: "Groovy Script",
            name: "registeredServiceMfaScript",
            paramName: "multifactorPolicy.script",
            required: false,
            containerId: "editServiceWizardMenuItemMfaPolicy",
            title: "Specifies the Groovy script location, inline or external, to determine multifactor authentication."
        });
    }
}

function handleMatchingStrategyChange(select) {
    const type = getLastWord($(select).val());
    console.log("Selected matching strategy type:", type);
    $(`#editServiceWizardMenuItemMatchingStrategy .${type}`).show();
    $("#editServiceWizardMenuItemMatchingStrategy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();

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
            }
        ],
        helpText: "Specifies the strategy to determine how service IDs are matched.",
        changeEventHandlers: "handleMatchingStrategyChange"
    }).data("renderer", function (value) {
        return {"@class": value};
    });
}

function handleAccessStrategyChange(select) {
    const type = getLastWord($(select).val());
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
    $("#editServiceWizardMenuItemAccessStrategy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAccessStrategy [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAccessStrategy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
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
                scriptFactoryAvailable
                    ? [{
                        value: "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy",
                        text: "GROOVY"
                    }]
                    : []
            ),
            ...(
                CAS_FEATURES.includes("SCIM")
                    ? [{
                        value: "org.apereo.cas.scim.v2.access.ScimRegisteredServiceAccessStrategy",
                        text: "SCIM"
                    }]
                    : []
            )
        ],
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
        containerField: "accessStrategy.headers",
        required: false
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
        containerField: "accessStrategy.headers",
        required: false
    });

    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide GroovyRegisteredServiceAccessStrategy",
            labelTitle: "Groovy Script",
            name: "registeredServiceAccessStrategyGroovyScript",
            paramName: "accessStrategy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemAccessStrategy",
            title: "Specifies the Groovy script location, inline or external, to determine access strategy."
        });
    }

    createInputField({
        cssClasses: "DefaultRegisteredServiceAccessStrategy",
        labelTitle: "Unauthorized Redirect URL",
        name: "registeredServiceAccessStrategyUnauthorizedRedirectUrl",
        paramName: "accessStrategy.unauthorizedRedirectUrl",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the URL to redirect to when access is unauthorized."
    });

    createMappedInputField({
        cssClasses: "DefaultRegisteredServiceAccessStrategy GrouperRegisteredServiceAccessStrategy ScimRegisteredServiceAccessStrategy",
        header: "Required Attributes",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyReqAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAccessStrategyReqAttrValue",
        valueLabel: "Attribute Value",
        containerField: "accessStrategy.requiredAttributes",
        multipleValues: true,
        multipleValuesType: "java.util.HashSet",
        required: false
    });

    createMappedInputField({
        cssClasses: "DefaultRegisteredServiceAccessStrategy",
        header: "Rejected Attributes",
        containerId: "editServiceWizardMenuItemAccessStrategy",
        keyField: "registeredServiceAccessStrategyRejectedAttrName",
        keyLabel: "Attribute Name",
        valueField: "registeredServiceAccessStrategyRejectedAttrValue",
        valueLabel: "Attribute Value",
        containerField: "accessStrategy.rejectedAttributes",
        multipleValues: true,
        multipleValuesType: "java.util.HashSet",
        required: false
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
        containerField: "accessStrategy.context",
        required: false
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
        cssClasses: "hide TimeBasedRegisteredServiceAccessStrategy",
        labelTitle: "Starting Date/Time",
        name: "registeredServiceAccessStrategyTimeBasedStartingDateTime",
        paramName: "accessStrategy.startingDateTime",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the starting date and time from which access is allowed. Supports expression language."
    });

    createInputField({
        cssClasses: "hide TimeBasedRegisteredServiceAccessStrategy",
        labelTitle: "Ending Date/Time",
        name: "registeredServiceAccessStrategyTimeBasedEndingDateTime",
        paramName: "accessStrategy.endingDateTime",
        required: false,
        containerId: "editServiceWizardMenuItemAccessStrategy",
        title: "Specifies the ending date and time after which access is denied. Supports expression language."
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
    $("#editServiceWizardMenuItemAuthenticationPolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemAuthenticationPolicy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
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
                scriptFactoryAvailable
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

    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide GroovyRegisteredServiceAuthenticationPolicyCriteria",
            labelTitle: "Groovy Script",
            name: "registeredServiceAuthenticationPolicyGroovyScript",
            paramName: "authenticationPolicy.criteria.script",
            required: false,
            containerId: "editServiceWizardMenuItemAuthenticationPolicy",
            title: "Groovy Script"
        });
    }

    createInputField({
        cssClasses: "AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria",
        labelTitle: "Required Authentication Handler(s)",
        name: "registeredServiceAuthenticationPolicyRequiredHandlers",
        paramName: "authenticationPolicy.requiredAuthenticationHandlers",
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
        valueLabel: "Property value(s) separated by comma",
        containerField: "properties",
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
                if (serviceDefinition["contacts"] && $input.val().length > 0) {
                    let currentContact = serviceDefinition["contacts"][1][index - 1];
                    if (!currentContact) {
                        currentContact = {
                            "@class": "org.apereo.cas.services.DefaultRegisteredServiceContact"
                        };
                        serviceDefinition["contacts"][1].push(currentContact);
                    }
                    currentContact[detail] = $input.val();
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
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemSSOParticipationPolicy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
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
            ...(
                scriptFactoryAvailable
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

    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide GroovyRegisteredServiceSingleSignOnParticipationPolicy",
            labelTitle: "Groovy Script",
            name: "registeredServiceSSOParticipationGroovyValue",
            paramName: "singleSignOnParticipationPolicy.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemSSOParticipationPolicy",
            title: "Specifies the Groovy script location, inline or external, to be used to generate the username."
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
        multipleValues: true,
        multipleValuesType: "java.util.ArrayList"
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
    
    $("#editServiceWizardMenuItemUsernameAttribute [id$='FieldContainer'] input")
        .val("");

    const hideDefaults = $(select).find(":selected").data("hideDefaults");
    if (hideDefaults) {
        hideElements($("#editServiceWizardMenuItemUsernameAttribute .DefaultRegisteredServiceUsernameProvider"));
    } else {
        showElements($("#editServiceWizardMenuItemUsernameAttribute .DefaultRegisteredServiceUsernameProvider"));
    }
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
                scriptFactoryAvailable
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


    CasDiscoveryProfile.fetchIfNeeded()
        .done(async () => {
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
            })

        });
    
    if (scriptFactoryAvailable) {
        createInputField({
            cssClasses: "hide GroovyRegisteredServiceUsernameProvider",
            labelTitle: "Groovy Script",
            name: "registeredServiceUsernameAttributeGroovyValue",
            paramName: "usernameAttributeProvider.groovyScript",
            required: false,
            containerId: "editServiceWizardMenuItemUsernameAttribute",
            title: "Specifies the Groovy script location, inline or external, to be used to generate the username."

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

    createInputField({
        cssClasses: "DefaultRegisteredServiceUsernameProvider",
        labelTitle: "Remove Pattern",
        name: "registeredServiceUsernameAttributeRemovePattern",
        paramName: "usernameAttributeProvider.removePattern",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies a regex pattern to remove from the username."
    });

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

    createInputField({
        cssClasses: "hide PairwiseOidcRegisteredServiceUsernameAttributeProvider",
        labelTitle: "Salt",
        name: "registeredServiceUsernameAttributePairwiseOidcSalt",
        paramName: "usernameAttributeProvider.persistentIdGenerator",
        required: false,
        containerId: "editServiceWizardMenuItemUsernameAttribute",
        title: "Specifies the salt value used to generate pairwise identifiers."

    }).data("renderer", function (value) {
        return {
            "@class": "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator",
            "salt": value.trim()
        };
    });

}

function createRegisteredServiceExpirationPolicy() {
    createInputField({
        cssClasses: "jquery-datepicker",
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy",
        labelTitle: "Expiration Date",
        name: "registeredServiceExpirationPolicyExpirationDate",
        paramName: "expirationPolicy.expirationDate",
        required: false,
        containerId: "editServiceWizardMenuItemExpirationPolicy",
        title: "Specifies the expiration date for the registered service in the format YYYY-MM-DD."
    });
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
        title: "Define a pattern that determines which requests are mapped to this application. This is typically a URL pattern or a regular regular expression."
    }).after(`
        <button class="mdc-button mdc-button--unelevated mdc-input-group-append mdc-icon-button mr-2" 
                type="button"
                onclick="validateServiceIdPattern(this)">
            <i class="mdi mdi-check-circle" aria-hidden="true"></i>
            <span class="sr-only">Validate</span>
        </button>
    `);
    ;

    createInputField({
        labelTitle: "Description",
        name: "registeredServiceDescription",
        paramName: "description",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Provide a brief description of the application."
    });

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

    if (value === "true" || value === true) {
        hideElements($("form#editServiceWizardForm .advanced-option"));
    } else {
        showElements($("form#editServiceWizardForm .advanced-option"));
        hideElements($("form#editServiceWizardForm [class*='class-']").not(`.class-${className}`).not(".always-show"));
    }

    if (availableMultifactorProviders.length === 0) {
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
}

function generateServiceDefinition() {
    setTimeout(function () {
        const editor = initializeAceEditor("wizardServiceEditor");
        let serviceDefinition = {
            "@class": $("#serviceClassType").text().trim()
        };

        $("form#editServiceWizardForm")
            .find("input,select")
            .each(function () {
                const $input = $(this);
                const paramName = $input.data("param-name");
                const paramType = $input.data("param-type");
                const skipWhenFalse = $input.data("param-skip-false");
                const skipWhenTrue = $input.data("param-skip-true");
                let value = $input.val();

                if (value && Array.isArray(value)) {
                    value = value.filter(v => v != null && v !== "").join(",");
                }
                if (paramName && paramName.trim().length > 0 && value && value.trim().length > 0) {
                    if (skipWhenFalse && (value === "false" || value === false)) {
                        console.debug(`Skipping parameter ${paramName} because its value is false`);
                    } else if (skipWhenTrue && (value === "true" || value === true)) {
                        console.debug(`Skipping parameter ${paramName} because its value is true`);
                    } else {
                        const renderer = $input.data("renderer");

                        if (value === "true") {
                            value = true;
                        } else if (value === "false") {
                            value = false;
                        } else if (isNumeric(value)) {
                            value = Number(value);
                        }

                        if (typeof renderer === "function") {
                            value = renderer(value, $input, serviceDefinition);
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
                                if (!current[part]) {
                                    const effectiveValue = (i === parts.length - 1) ? value : {};
                                    if (typeof effectiveValue !== "object"
                                        || (typeof effectiveValue === "object" && Object.keys(effectiveValue).length > 0)) {
                                        current[part] = effectiveValue;
                                    }
                                }

                                current = current[part];
                            }
                        } else {
                            serviceDefinition[paramName] = value;
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
            let remainingKeys = Object.keys(serviceDefinition);
            if (remainingKeys.length === 1 && remainingKeys[0] === "@class") {
                delete serviceDefinition["@class"];
                editor.setValue("");
            } else {
                editor.setValue(JSON.stringify(serviceDefinition, null, 4));
            }
        } else {
            editor.setValue("");
        }
        editor.gotoLine(1);
    }, 100);
}

function generateMappedFieldValue(sectionId, config) {
    const {
        multipleValues = false,
        valueFieldRenderer,
        multipleValuesType = "java.util.ArrayList",
        unwrapSingleElement = false
    } = config;

    const definition = {};

    const inputs = $(`#${sectionId}`).find("input");
    for (let i = 0; i < inputs.length; i += 2) {
        const key = $(inputs[i]).val();
        const value = $(inputs[i + 1]).val();

        if (valueFieldRenderer !== undefined && typeof valueFieldRenderer === "function") {
            definition[key] = valueFieldRenderer($(inputs[i]), $(inputs[i + 1]));
        } else if (key && key.trim().length > 0 && value && value.trim().length > 0) {
            if (multipleValues) {
                const valueArray = value.split(",").filter(s => s.trim().length > 0);
                if (unwrapSingleElement && valueArray.length === 1) {
                    definition[key] = value;
                } else {
                    definition[key] = [multipleValuesType, valueArray];
                }
            } else {
                definition[key] = value;
            }
        }
    }
    return {"@class": "java.util.TreeMap", ...definition};
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
        unwrapSingleElement = false
    } = config;

    const sectionContainerId = `registeredService${capitalize(keyField)}MapContainer`;
    const addButtonId = `registeredService${capitalize(keyField)}AddButton`;
    const deleteAllButtonId = `registeredService${capitalize(keyField)}DeleteAllButton`;
    const removeButtonId = `registeredService${capitalize(keyField)}RemoveButton`;
    const mapRowId = `registeredService${capitalize(keyField)}Row`;

    const inputFieldKeyId = `registeredService${capitalize(keyField)}`;
    const inputFieldValueId = `registeredService${capitalize(valueField)}`;
    const rowElements = `
            <div class="d-flex justify-content-between pt-2 ${keyField}-map-row ${cssClasses}" id="${mapRowId}">
                <label for="${keyField}"
                       class="mdc-text-field mdc-text-field--outlined mdc-text-field--with-trailing-icon control-label ${cssClasses}">
                    <span class="mdc-notched-outline pr-2">
                        <span class="mdc-notched-outline__leading"></span>
                        <span class="mdc-notched-outline__notch">
                            <span class="mdc-floating-label">${keyLabel ?? ""}</span>
                        </span>
                        <span class="mdc-notched-outline__trailing"></span>
                    </span>
                    <input class="mdc-text-field__input form-control "
                           id="${inputFieldKeyId}"
                           name="${inputFieldKeyId}"
                           size="25"
                           type="text"
                           data-param-name="${containerField}"
                           data-param-type="${containerType}"
                           ${required ? "required" : ""}/>
                </label>

                <label for="${valueField}"
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
                           ${required ? "required" : ""}/>
                </label>

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

    $(`#${containerId}`).append($(`${html}`));

    function configureRemoveMapRowEventHandler() {
        $(`button[name=${removeButtonId}]`).off().on("click", function () {
            $(this).closest(`.${keyField}-map-row`).remove();
            generateServiceDefinition();
        });
    }

    function configureInputEventHandler() {
        $(`#${sectionContainerId} input`).off().on("input", () => {
            generateServiceDefinition();
        });
    }

    function configureInputRenderer() {
        $(`#${sectionContainerId} input`).data("renderer", function () {
            return generateMappedFieldValue(sectionContainerId, config);
        });
    }

    $(`button[name=${addButtonId}]`).off().on("click", () => {
        let elementsToAdd = $(rowElements).removeClass("hide");
        elementsToAdd.find("*").removeClass("hide");

        $(`#${sectionContainerId}ToAppend`).append(elementsToAdd);
        configureRemoveMapRowEventHandler();
        cas.attachFields();
        configureInputEventHandler();
        configureInputRenderer();
    });
    $(`button[name=${deleteAllButtonId}]`).off().on("click", () => {
        $(`#${sectionContainerId}ToAppend`).empty();
        $(`#${sectionContainerId} input`).val("");
        configureRemoveMapRowEventHandler();
        cas.attachFields();
        configureInputEventHandler();
        configureInputRenderer();
        generateServiceDefinition();
    });

    configureRemoveMapRowEventHandler();
    configureInputEventHandler();
    configureInputRenderer();
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

    const container = $("<span>", {
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
        dropdownParent: "body",
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
    }).on("input", function () {
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

function openRegisteredServiceWizardDialog() {
    function openWizardDialog(serviceClass) {
        $("#editServiceWizardGeneralContainer").find("input").val("");
        $('.jqueryui-multiselectmenu').each(function () {
            this.tomselect?.clear();
        });
        
        const editor = initializeAceEditor("wizardServiceEditor");
        editor.setReadOnly(true);
        editor.setValue("");
        editor.gotoLine(1);

        const className = getLastWord(serviceClass);
        $("#serviceClassType").text(serviceClass);
        $("#editServiceWizardForm").data("service-class", serviceClass).data("service-class-name", className);

        $("#editServiceWizardMenu")
            .accordion({
                collapsible: true,
                heightStyle: "content",
                activate: function (event, ui) {
                    let idx = $("#editServiceWizardMenu").accordion("option", "active");
                    if (isNumeric(idx)) {
                        // console.log(`Saving wizard menu index ${idx}`);
                        localStorage.setItem("registeredServiceWizardMenuOption", idx);
                    }
                }
            });

        $(`.class-${className}`).show();
        $("[class*='class-']").not(`.class-${className}`).hide();

        $("#editServiceWizardMenu").accordion("refresh");

        const editServiceWizardDialogElement = document.getElementById("editServiceWizardDialog");
        editServiceWizardDialog = window.mdc.dialog.MDCDialog.attachTo(editServiceWizardDialogElement);
        $(editServiceWizardDialogElement).attr("newService", true);
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
                $(`button#${id}Button`).on("click", function (e) {
                    setTimeout(() => generateServiceDefinition(), 0);
                });
            });

        $("#editServiceWizardForm input").val("");
        $("#editServiceWizardForm option").filter(function () {
            const clazz = $(this).data("serviceClass");
            return clazz !== undefined && clazz !== null;
        }).remove();

        const dropdown = $("#editServiceWizardForm select.jqueryui-selectmenu");
        try {
            dropdown.selectmenu("refresh");
        } catch (e) {
            dropdown.selectmenu();
        }

        generateServiceDefinition();
        editServiceWizardDialog["open"]();

        const value = $("#hideAdvancedOptions").val();
        if (value === "false" || value === false) {
            $("#hideAdvancedOptionsButton").click();
        }
        hideAdvancedRegisteredServiceOptions();

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
                const updated = original.replace("/ OpenID Connect ", "");
                $(this).text(updated);
            });
            $("#editServiceWizardMenu").accordion("refresh");
            break;
        case "OidcRegisteredService":
            $("#registeredServiceIdLabel span.mdc-floating-label").text("Redirect URI");
            $(`h3.class-${className}`).each(function () {
                const original = $(this).text();
                const updated = original.replace("OAuth / ", "");
                $(this).text(updated);
            });
            $("#editServiceWizardMenu").accordion("refresh");
            createOidcRegisteredServiceFields();
            break;
        }
        let savedIndex = localStorage.getItem("registeredServiceWizardMenuOption");
        if (savedIndex !== null && isNumeric(savedIndex)) {
            savedIndex = Number(savedIndex);
        } else {
            savedIndex = 0;
        }

        const visible = $("#editServiceWizardForm").find(`.ui-accordion-header:eq(${savedIndex})`).is(":visible");
        if (!visible) {
            savedIndex = 0;
        }
        $("#editServiceWizardMenu").accordion("option", "active", savedIndex);
        setTimeout(function () {
            $("#editServiceWizardForm input:visible:enabled").first().focus();
        }, 200);
    }

    if (Object.keys(supportedServiceTypes).length === 1) {
        openWizardDialog(Object.keys(supportedServiceTypes)[0]);
    } else {
        const sortedServiceTypes = Object.keys(supportedServiceTypes)
            .sort()
            .reduce((acc, key) => {
                acc[key] = supportedServiceTypes[key];
                return acc;
            }, {});
        Swal.fire({
            title: "What type of application do you want to add?",
            input: "select",
            icon: "question",
            inputOptions: sortedServiceTypes,
            showCancelButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                openWizardDialog(result.value);
            }
        });
    }
}

