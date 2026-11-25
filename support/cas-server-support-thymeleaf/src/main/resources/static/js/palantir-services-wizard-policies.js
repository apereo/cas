/* The wizard's other sections: properties, contacts, public key, acceptable usage, single sign-on, interrupts, delegated authentication, activation, usernames and expiration. */

function createRegisteredServiceProperties() {
    createMappedInputField({
        containerId: "editServiceWizardMenuItemProperties",
        keyField: "registeredServicePropertyName",
        keyLabel: "Property Name",
        valueField: "registeredServicePropertyValue",
        valueLabel: "Property Value(s) Separated by Comma",
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
