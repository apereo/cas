function createRegisteredServiceSaml2Fields() {
    createInputField({
        labelTitle: "Metadata Location",
        name: "registeredServiceMetadataLocation",
        paramName: "metadataLocation",
        required: true,
        containerId: "editServiceWizardSamlContainer",
        title: "Define the metadata location (URL, file path, etc) for the SAML2 service provider."
    });
    createSamlMetadataUploadButton();
    PalantirSamlMetadataSources.watch();

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Identity Provider Metadata Location",
        name: "registeredServiceIdPMetadataLocation",
        paramName: "idpMetadataLocation",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Directory location of SAML2 identity provider metadata available to CAS as an override."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Proxy Location",
        name: "registeredServiceMetadataProxyLocation",
        paramName: "metadataProxyLocation",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Proxy endpoint (i.e. <code>https://proxy-address:8901</code>) to fetch service metadata from URL resources."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Max Validity",
        name: "registeredServiceMetadataMaxValidity",
        paramName: "metadataMaxValidity",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a filter that requires the presence of a <code>ValidUntil</code> attribute on the root element of the metadata document."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Signature Location",
        name: "registeredServiceMetadataSignatureLocation",
        paramName: "metadataSignatureLocation",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Location of the metadata signing certificate/public key to validate the metadata which must be defined from system files or classpath. If defined, will enforce the <code>SignatureValidationFilter</code> validation filter on metadata."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Required Authentication Context Class",
        name: "registeredServiceRequiredAuthenticationContextClass",
        paramName: "requiredAuthenticationContextClass",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, will specify the SAML authentication context class in the final response."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Required NameID Format",
        name: "registeredServiceRequiredNameIdFormat",
        paramName: "requiredNameIdFormat",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, will force the indicated Name ID format in the final SAML response. "
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "NameID Qualifier",
        name: "registeredServiceNameIdQualifier",
        paramName: "nameIdQualifier",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the NameID qualifier to be used in SAML responses."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Service Provider NameID Qualifier",
        name: "registeredServiceSPNameIdQualifier",
        paramName: "serviceProviderNameIdQualifier",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the Service Provider NameID qualifier to be used in SAML responses."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Issuer Entity ID",
        name: "registeredServiceIssuerEntityId",
        paramName: "issuerEntityId",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, will override the issuer value with the given identity provider entity id. This may be useful in cases where CAS needs to maintain multiple identity provider entity ids."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Subject Locality",
        name: "registeredServiceSubjectLocality",
        paramName: "subjectLocality",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, will overwrite the <code>SubjectLocality</code> attribute of the SAML2 authentication statement."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Logout Response Binding",
        name: "registeredServiceLogoutResponseBinding",
        paramName: "logoutResponseBinding",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the logout response binding to use when sending SAML logout responses to the service provider."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Assertion Audience",
        name: "registeredServiceAssertionAudience",
        paramName: "assertionAudiences",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of audience values to include in SAML assertions."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Encryptable Attributes",
        name: "registeredServiceEncryptableAttributes",
        paramName: "encryptableAttributes",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of attribute names that should be encrypted in SAML assertions."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Skew Allowance",
        name: "registeredServiceSkewAllowance",
        paramName: "skewAllowance",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, indicates number of seconds used to skew authentication dates such as valid-from and valid-until elements, etc."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Validity Until",
        name: "registeredServiceValidityUntil",
        paramName: "validityUntil",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "If defined, indicates the number of seconds from now used for the valid-until elements"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Signature Reference Digest Methods",
        name: "registeredServiceSigningSignatureReferenceDigestMethods",
        paramName: "signingSignatureReferenceDigestMethods",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of digest methods to use when signing SAML messages."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Signature Algorithms",
        name: "registeredServiceSigningSignatureAlgorithms",
        paramName: "signingSignatureAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of signature algorithms to use when signing SAML messages."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Signature Blacklisted Algorithms",
        name: "registeredServiceSigningSignatureBlackListedAlgorithms",
        paramName: "signingSignatureBlackListedAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of blacklisted signature algorithms to avoid when signing SAML messages."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Signature Whitelisted Algorithms",
        name: "registeredServiceSigningSignatureWhiteListedAlgorithms",
        paramName: "signingSignatureWhiteListedAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of whitelisted signature algorithms to allow when signing SAML messages."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Whitelist/Blacklist Precedence",
        name: "registeredServiceWhiteListBlackListPrecedence",
        paramName: "whiteListBlackListPrecedence",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the precedence of whitelists/blacklists when selecting algorithms for signing/encryption. Accepted values are <code>INCLUDE</code> or <code>EXCLUDE</code>."
    })
        .css("text-transform", "uppercase")
        .on("input", function () {
            const value = $(this).val().toUpperCase();
            $(this).val(value);
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Encryption Data Algorithms",
        name: "registeredServiceEncryptionDataAlgorithms",
        paramName: "encryptionDataAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of data encryption algorithms to use when encrypting SAML assertions."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Encryption Key Algorithms",
        name: "registeredServiceEncryptionKeyAlgorithms",
        paramName: "encryptionKeyAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of key encryption algorithms to use when encrypting SAML assertions."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Encryption Blacklisted Algorithms",
        name: "registeredServiceEncryptionBlackListedAlgorithms",
        paramName: "encryptionBlackListedAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of blacklisted algorithms to avoid when encrypting SAML assertions."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });
    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Encryption Whitelisted Algorithms",
        name: "registeredServiceEncryptionWhiteListedAlgorithms",
        paramName: "encryptionWhiteListedAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of whitelisted algorithms to allow when encrypting SAML assertions."
    })
        .data("renderer", function (value) {
            return ["java.util.ArrayList", value.split(",").filter(v => v != null && v !== "")];
        });


    createSelectField({
        containerId: "editServiceWizardSamlContainer",
        labelTitle: "Sign Assertions:",
        paramName: "signAssertions",
        options: [
            {value: "UNDEFINED", text: "UNDEFINED"},
            {value: "TRUE", text: "TRUE"},
            {value: "", text: "FALSE", selected: true}
        ],
        helpText: "Specify whether SAML assertions should be signed for this service provider."
    });

    createSelectField({
        containerId: "editServiceWizardSamlContainer",
        labelTitle: "Sign Responses:",
        paramName: "signResponses",
        options: [
            {value: "UNDEFINED", text: "UNDEFINED"},
            {value: "", text: "TRUE", selected: true},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specify whether SAML assertions should be signed for this service provider."
    });

    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardSamlContainer",
        labelTitle: "Sign Logout Responses:",
        paramName: "signLogoutResponse",
        options: [
            {value: "", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specify whether SAML assertions should be signed for this service provider."
    });

    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardSamlContainer",
        labelTitle: "Sign Logout Requests:",
        paramName: "signLogoutRequest",
        options: [
            {value: "", text: "UNDEFINED", selected: true},
            {value: "TRUE", text: "TRUE"},
            {value: "FALSE", text: "FALSE"}
        ],
        helpText: "Specify whether SAML assertions should be signed for this service provider."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Criteria Direction",
        name: "registeredServiceMetadataCriteriaDirection",
        paramName: "metadataCriteriaDirection",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the metadata criteria direction (e.g. INCLUDE or EXCLUDE)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Criteria Pattern",
        name: "registeredServiceMetadataCriteriaPattern",
        paramName: "metadataCriteriaPattern",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the pattern used to filter metadata entities according to the criteria."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Expiration Duration",
        name: "registeredServiceMetadataExpirationDuration",
        paramName: "metadataExpirationDuration",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines how long metadata is considered valid (e.g., an ISO-8601 duration like PT60M)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Credential Fingerprint",
        name: "registeredServiceSigningCredentialFingerprint",
        paramName: "signingCredentialFingerprint",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Specifies the fingerprint of the signing credential used for SAML signing operations."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Key Algorithm",
        name: "registeredServiceSigningKeyAlgorithm",
        paramName: "signingKeyAlgorithm",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the algorithm used for signing keys"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Metadata Criteria Roles",
        name: "registeredServiceMetadataCriteriaRoles",
        paramName: "metadataCriteriaRoles",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines the metadata role(s) used when filtering entities (i.e. `SPSSODescriptor`, `IDPSSODescriptor`)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signing Credential Type",
        name: "registeredServiceSigningCredentialType",
        paramName: "signingCredentialType",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Specifies the type of signing credential used (e.g., X509, BASIC)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Signature Canonicalization Algorithm",
        name: "registeredServiceSigningSignatureCanonicalizationAlgorithm",
        paramName: "signingSignatureCanonicalizationAlgorithm",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines which canonicalization algorithm is used for XML signature processing."
    });
}

function createRegisteredServiceMetadataCriteriaEntityAttribute() {
    createMappedInputField({
        containerId: "editServiceWizardSamlMetadataCriteriaEntityAttributesContainer",
        keyField: "metadataCriteriaEntityAttribute",
        keyLabel: "Entity Attribute",
        valueField: "metadataCriteriaEntityAttributeValue",
        valueLabel: "Entity attribute value(s) separated by comma (i.e. <code>http://refeds.org/category/research-and-scholarship</code>)",
        containerField: "metadataCriteriaEntityAttributes",
        multipleValues: true
    });
}

function createRegisteredServiceAttributeNameFormat() {
    createMappedInputField({
        containerId: "editServiceWizardSamlAttributeNameFormatsContainer",
        keyField: "attributeName-format",
        keyLabel: "Attribute Name",
        valueField: "attributeFormatValue",
        valueLabel: "Attribute Format (i.e. basic, uri, unspecified, etc)",
        containerField: "attributeNameFormats"
    });
}

function createRegisteredServiceAttributeValueTypes() {
    createMappedInputField({
        containerId: "editServiceWizardSamlAttributeValueTypesContainer",
        keyField: "attributeName-valueType",
        keyLabel: "Attribute Name",
        valueField: "attributeValueType",
        valueLabel: "Attribute Value Type (i.e. <code>XSString, XSURI, NameIDType</code>, etc)",
        containerField: "attributeValueTypes"
    });
}

function createRegisteredServiceAttributeFriendlyNames() {
    createMappedInputField({
        containerId: "editServiceWizardSamlAttributeFriendlyNamesContainer",
        keyField: "attributeName-friendly",
        keyLabel: "Attribute Name",
        valueField: "attributeFriendlyName",
        valueLabel: "Attribute Friendly Name",
        containerField: "attributeFriendlyNames"
    });
}

function createSamlRegisteredServiceAttributeReleasePolicy() {
    const $registeredServiceAttributeReleasePolicy = $("#registeredServiceAttributeReleasePolicy");

    const releasePolicyOptions = [
        {
            value: "org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy",
            text: "INCOMMON R&S",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.RefedsRSAttributeReleasePolicy",
            text: "REFEDS R&S",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.MetadataRequestedAttributesAttributeReleasePolicy",
            text: "METADATA REQUESTED ATTRIBUTES",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy",
            text: "METADATA ENTITY ATTRIBUTES",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.AuthnRequestRequestedAttributesAttributeReleasePolicy",
            text: "AUTHN REQUEST REQUESTED ATTRIBUTES",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.AuthnRequestRequesterIdAttributeReleasePolicy",
            text: "AUTHN REQUEST REQUESTER ID",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.MetadataRegistrationAuthorityAttributeReleasePolicy",
            text: "METADATA REGISTRATION AUTHORITY",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.AnonymousAccessAttributeReleasePolicy",
            text: "ANONYMOUS ACCESS",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.PseudonymousAccessAttributeReleasePolicy",
            text: "PSEUDONYMOUS ACCESS",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.PersonalizedAccessAttributeReleasePolicy",
            text: "PERSONALIZED ACCESS",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.MetadataEntityGroupAttributeReleasePolicy",
            text: "METADATA ENTITY GROUP",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicy",
            text: "PATTERN MATCHING ENTITY IDS",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.AttributeQueryAttributeReleasePolicy",
            text: "ATTRIBUTE QUERY",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.MappedEntityAttributesAttributeReleasePolicy",
            text: "MAPPED ENTITY ATTRIBUTES",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        {
            value: "org.apereo.cas.support.saml.services.EduPersonTargetedIdAttributeReleasePolicy",
            text: "EDUPERSON TARGETED ID",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        ...(
            PalantirDashboardConfiguration.scriptFactoryAvailable()
                ? [{
                    value: "org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicy",
                    text: "GROOVY SAML"
                }]
                : []
        ),
    ];

    releasePolicyOptions.forEach(option => {
        if ($registeredServiceAttributeReleasePolicy.find(`option[value="${option.value}"]`).length === 0) {
            appendOptionsToDropDown({
                selectElement: $registeredServiceAttributeReleasePolicy,
                options: [option]
            });
        }
    });

    $registeredServiceAttributeReleasePolicy.selectmenu("refresh");



}

/**
 * Metadata locations that are sources of many service providers: the stores of the SAML2 metadata managers, such as
 * MongoDB or Amazon S3, and metadata query (MDQ) servers. When such a location is chosen for an application with no
 * entity id yet, the operator is asked whether the application should accept every entity the source provides, with
 * <code>.+</code> as its entity id, or name its entity ids.
 */
class PalantirSamlMetadataSources {
    static TRUST_EVERY_ENTITY = ".+";

    /**
     * Sources that only find the metadata of the entity an application names, so an application cannot accept all of
     * them with a pattern: the JSON file of service providers looks each one up by its exact entity id, and metadata
     * kept in memory is not resolved at all.
     */
    static ONE_ENTITY_AT_A_TIME = ["json://", "memory://"];

    static asked = "";

    /**
     * Ask about a location once it is chosen or typed, not while the wizard is being filled.
     */
    static watch() {
        PalantirSamlMetadataSources.asked = "";
        $("#registeredServiceMetadataLocation").off("change.palantirSources").on("change.palantirSources", async function () {
            if (typeof isPopulatingWizard !== "undefined" && isPopulatingWizard) {
                return;
            }
            await PalantirSamlMetadataSources.offer(String($(this).val() ?? "").trim());
        });
    }

    /**
     * Whether the wizard still holds what a question was asked about: the same metadata location and entity id, in a
     * wizard that is still open. An answer to a question about a location since changed, or an entity id since typed,
     * is not applied.
     *
     * @param location the metadata location asked about
     * @param entityId the entity id when asked
     * @returns {boolean} true when the answer still applies
     */
    static unchanged(location, entityId) {
        return $("#editServiceWizardDialog").dialog("instance") !== undefined && $("#editServiceWizardDialog").dialog("isOpen")
            && String($("#registeredServiceMetadataLocation").val() ?? "").trim() === location
            && String($("#registeredServiceId").val() ?? "").trim() === entityId;
    }

    static isMetadataQuery(location) {
        return /\/entities\/\{0}$/.test(location);
    }

    /**
     * The source a location reads many service providers from: a metadata manager CAS has, or one CAS can have when it
     * cannot be asked, or a metadata query server.
     *
     * @param location the metadata location
     * @returns {Promise<string|null>} what the source is, for people, or null when the location is not such a source
     */
    static async sourceOf(location) {
        const lower = location.toLowerCase();
        if (location === "" || PalantirSamlMetadataSources.ONE_ENTITY_AT_A_TIME.some(prefix => lower.startsWith(prefix))) {
            return null;
        }
        if (PalantirSamlMetadataSources.isMetadataQuery(location)) {
            return "This metadata query (MDQ) server";
        }
        const managers = typeof PalantirSamlMetadataUpload !== "undefined" && CasActuatorEndpoints.samlIdpRegisteredServiceMetadata()
            ? await PalantirSamlMetadataUpload.loadManagers()
            : [];
        const manager = managers.find(entry => entry?.sourceId && lower.startsWith(String(entry.sourceId).toLowerCase()));
        if (manager) {
            return `The ${manager.name ?? manager.sourceId} metadata store`;
        }
        const known = Object.keys(PalantirWizardSuggestions.SAML_METADATA_MANAGERS).find(prefix => lower.startsWith(prefix));
        return known ? `The ${known} metadata store` : null;
    }

    /**
     * Ask whether the application accepts every entity of the source its metadata location names, once per location,
     * when it has no entity id yet: yes makes its entity id <code>.+</code>; no leaves the entity id to be entered.
     *
     * @param location the metadata location
     * @returns {Promise<void>} resolved once answered
     */
    static async offer(location) {
        const entityId = $("#registeredServiceId");
        const current = String(entityId.val() ?? "").trim();
        if (location === PalantirSamlMetadataSources.asked || (current !== "" && current !== PalantirSamlMetadataSources.TRUST_EVERY_ENTITY)) {
            return;
        }
        const source = await PalantirSamlMetadataSources.sourceOf(location);
        if (source === null || !PalantirSamlMetadataSources.unchanged(location, current)) {
            return;
        }
        PalantirSamlMetadataSources.asked = location;
        const escape = escapePalantirMarkup;
        const answer = await Swal.fire({
            icon: "question",
            titleText: "Accept Every Service Provider From This Source?",
            html: `<p>${escape(source)} (<code>${escape(location)}</code>) can provide metadata for many service providers.</p>
                <p>If you accept all of them, CAS will trust every service provider whose metadata it finds there:
                they need no application of their own, and the policies of this application, such as the attributes it
                releases, apply to every one of them. Its entity ID becomes <code>${PalantirSamlMetadataSources.TRUST_EVERY_ENTITY}</code>.</p>
                <p>Otherwise, enter the entity ID, or a pattern of entity IDs, that you can accept from this source.</p>`,
            showCancelButton: true,
            confirmButtonText: "Accept Every Service Provider",
            cancelButtonText: "Enter Entity IDs",
            focusCancel: true
        });
        if (!PalantirSamlMetadataSources.unchanged(location, current)) {
            return;
        }
        if (answer.isConfirmed) {
            entityId.val(PalantirSamlMetadataSources.TRUST_EVERY_ENTITY).trigger("input").trigger("blur");
            entityId.closest(".mdc-text-field").find(".mdc-floating-label").addClass("mdc-floating-label--float-above");
            return;
        }
        if (answer.dismiss !== Swal.DismissReason.cancel) {
            return;
        }
        if (current === PalantirSamlMetadataSources.TRUST_EVERY_ENTITY) {
            entityId.val("").trigger("input");
        }
        entityId.trigger("focus");
    }
}

/**
 * Offer Upload Metadata and Manage Metadata buttons next to the metadata location, when SAML2 metadata management is
 * available: its endpoint can be written to and at least one metadata manager is registered. Uploaded metadata goes to
 * a metadata manager, the same way as from the metadata management section under Protocols, and the metadata location
 * then names that manager's source, such as <code>mongodb://</code>; an empty entity id is taken from the uploaded
 * metadata, asking which service provider the application is when the metadata holds several. Manage Metadata closes
 * the wizard, asking first as usual when it holds unsaved changes, and opens that section with the manager of the
 * metadata location, or the first manager, and its entries.
 */
function createSamlMetadataUploadButton() {
    const container = $("#registeredServiceMetadataLocationFieldContainer");
    if (container.length === 0 || $("#registeredServiceMetadataUpload").length > 0) {
        return;
    }
    const button = $("<button>", {
        type: "button",
        id: "registeredServiceMetadataUpload",
        class: "mdc-button mdc-button--outlined palantir-wizard-field__action",
        title: "Upload the service provider's metadata to a SAML2 metadata manager",
        "data-palantir-write": "samlIdPRegisteredServiceMetadata",
        hidden: true
    }).append($("<i>", {class: "mdi mdi-upload", "aria-hidden": "true"}))
        .append($("<span>", {class: "mdc-button__label", text: "Upload Metadata"}));
    const manage = $("<button>", {
        type: "button",
        id: "registeredServiceMetadataManage",
        class: "mdc-button mdc-button--outlined palantir-wizard-field__action",
        title: "Close the wizard and show the metadata entries of the SAML2 metadata managers",
        hidden: true
    }).append($("<i>", {class: "mdi mdi-database-cog-outline", "aria-hidden": "true"}))
        .append($("<span>", {class: "mdc-button__label", text: "Manage Metadata"}));
    container.addClass("palantir-wizard-field--with-action").append(button, manage);
    if (!PalantirSamlMetadataUpload.available()) {
        return;
    }
    PalantirSamlMetadataUpload.loadManagers().then(managers => button.add(manage).prop("hidden", managers.length === 0));
    manage.on("click", async () => {
        const managers = await PalantirSamlMetadataUpload.loadManagers();
        const location = String($("#registeredServiceMetadataLocation").val() ?? "").trim();
        const manager = managers.find(entry => location !== "" && location.startsWith(entry.sourceId)) ?? managers[0];
        if (!manager) {
            return;
        }
        const dialog = $("#editServiceWizardDialog");
        dialog.off(".palantirManageMetadata")
            .one("dialogclose.palantirManageMetadata", () => {
                dialog.off(".palantirManageMetadata");
                showSamlMetadataManager(manager.sourceId);
            })
            .one("palantir:closecancelled.palantirManageMetadata", () => dialog.off(".palantirManageMetadata"));
        dialog.dialog("close");
    });
    button.on("click", async () => {
        const managers = await PalantirSamlMetadataUpload.loadManagers();
        const location = String($("#registeredServiceMetadataLocation").val() ?? "").trim();
        const current = managers.filter(manager => location !== "" && location.startsWith(manager.sourceId));
        PalantirSamlMetadataUpload.open({
            managers: [...current, ...managers.filter(manager => !current.includes(manager))],
            name: String($("#registeredServiceName").val() ?? "").trim(),
            onUploaded: async (manager, document) => {
                $("#registeredServiceMetadataLocation").val(manager.sourceId).trigger("input").trigger("blur");
                let providers = [];
                try {
                    providers = PalantirSamlMetadataNames.serviceProviders(PalantirSamlMetadataNames.parse(document.value ?? ""))
                        .filter(provider => provider.getAttribute("entityID"));
                } catch (error) {
                    console.debug("The entity ids could not be read from the uploaded metadata", error);
                }
                let entityId = String($("#registeredServiceId").val() ?? "").trim();
                if (entityId === "") {
                    const entityIds = providers.map(provider => provider.getAttribute("entityID"));
                    entityId = (entityIds.length > 1 ? await PalantirSamlMetadataImport.choose(entityIds) : entityIds[0]) ?? "";
                    if (entityId) {
                        $("#registeredServiceId").val(entityId).trigger("input").trigger("blur");
                    }
                }
                const provider = providers.find(entry => entry.getAttribute("entityID") === entityId);
                if (provider && String($("#registeredServiceName").val() ?? "").trim() === "") {
                    $("#registeredServiceName").val(PalantirSamlMetadataNames.nameOf(provider)).trigger("input").trigger("blur");
                }
            }
        });
    });
}

/**
 * Fill the SAML2 wizard from the metadata of a service provider. The metadata comes from a URL, read by the server
 * since browsers are not allowed to read most metadata URLs, or from XML the operator pastes or chooses. An aggregate
 * asks which of its service providers to use. What the metadata says is merged over what the wizard holds:
 * the entity id, the name, description, information and privacy URLs and logo the provider publishes, the metadata
 * location, its NameID format when it declares one, assertion signing when it wants assertions signed, an
 * attribute release policy for its research and scholarship category or the attributes it requests, and its
 * contacts. Nothing is saved.
 */
class PalantirSamlMetadataImport {
    static ENTITY_CATEGORY = "http://macedir.org/entity-category";

    static CATEGORY_POLICIES = {
        "http://refeds.org/category/research-and-scholarship": "org.apereo.cas.support.saml.services.RefedsRSAttributeReleasePolicy",
        "http://id.incommon.org/category/research-and-scholarship": "org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy"
    };

    static REQUESTED_ATTRIBUTES_POLICY = "org.apereo.cas.support.saml.services.MetadataRequestedAttributesAttributeReleasePolicy";

    /**
     * Whether the server can read metadata from a URL for the wizard.
     *
     * @returns {boolean} true when the SAML2 metadata cache endpoint is available
     */
    static canReadUrls() {
        return Boolean(CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache());
    }

    /**
     * Ask for the metadata, read it and fill the wizard.
     *
     * @returns {Promise<void>} resolved once the wizard is filled or the operator gave up
     */
    static async open() {
        const location = String($("#registeredServiceMetadataLocation").val() ?? "").trim();
        const canReadUrls = PalantirSamlMetadataImport.canReadUrls();
        const result = await Swal.fire({
            title: "Fill from Service Provider Metadata",
            html: `
                <form class="palantir-saml-metadata-import" onsubmit="return false;">
                    <label for="samlMetadataImportUrl">Metadata URL</label>
                    <input id="samlMetadataImportUrl" type="url" class="swal2-input" placeholder="https://sp.example.org/metadata"
                           value="${/^https?:\/\//i.test(location) ? escapePalantirMarkup(location) : ""}" ${canReadUrls ? "" : "disabled"}>
                    <p class="palantir-saml-metadata-import__hint">${canReadUrls
        ? "CAS reads the metadata at this URL, which also becomes the application's metadata location."
        : "Reading a URL needs the SAML2 metadata cache endpoint, which is not available; paste or choose the metadata instead."}</p>
                    <label for="samlMetadataImportXml">Or Paste the Metadata XML</label>
                    <textarea id="samlMetadataImportXml" class="swal2-textarea" rows="5" spellcheck="false"></textarea>
                    <label for="samlMetadataImportFile">Or Choose a Metadata File</label>
                    <label class="palantir-saml-metadata-import__file" for="samlMetadataImportFile">
                        <input id="samlMetadataImportFile" type="file" class="palantir-saml-metadata-import__file-input"
                               accept=".xml,application/xml,text/xml">
                        <i class="mdi mdi-file-upload-outline" aria-hidden="true"></i>
                        <span class="palantir-saml-metadata-import__file-text">
                            <strong id="samlMetadataImportFileName">Choose an XML File</strong>
                            <span id="samlMetadataImportFileHint">or drop it here</span>
                        </span>
                    </label>
                </form>`,
            width: Math.min(720, $(window).width() - 32),
            showCancelButton: true,
            confirmButtonText: "Read Metadata",
            focusConfirm: false,
            didOpen: () => PalantirSamlMetadataImport.trackFileChoice(),
            preConfirm: () => {
                const url = String($("#samlMetadataImportUrl").val() ?? "").trim();
                const xml = String($("#samlMetadataImportXml").val() ?? "").trim();
                const file = $("#samlMetadataImportFile")[0]?.files?.[0] ?? null;
                if (!url && !xml && !file) {
                    Swal.showValidationMessage("Enter a metadata URL, or paste or choose the metadata XML.");
                    return false;
                }
                return {url: url, xml: xml, file: file};
            }
        });
        if (!result.isConfirmed) {
            return;
        }
        try {
            const source = await PalantirSamlMetadataImport.read(result.value);
            if (source) {
                await PalantirSamlMetadataImport.fill(source.entity, source.metadataLocation);
            }
        } catch (error) {
            PalantirDialogs.error("The Metadata Could Not Be Read", error.message);
        }
    }

    /**
     * Show the chosen file's name in the file drop zone, and mark the zone while a file is dragged over it.
     */
    static trackFileChoice() {
        const zone = $(".palantir-saml-metadata-import__file");
        $("#samlMetadataImportFile")
            .on("change", function () {
                const file = this.files?.[0];
                $("#samlMetadataImportFileName").text(file ? file.name : "Choose an XML File");
                $("#samlMetadataImportFileHint").text(file ? "Choose or drop another file to replace it" : "or drop it here");
                zone.toggleClass("has-file", Boolean(file)).removeClass("is-dragover");
            })
            .on("dragenter dragover", () => zone.addClass("is-dragover"))
            .on("dragleave drop", () => zone.removeClass("is-dragover"));
    }

    /**
     * Read the metadata and find the service provider to fill the wizard from.
     *
     * @param request the URL, pasted XML or chosen file
     * @returns {Promise<{entity: Element, metadataLocation: string|null}|null>} the provider's EntityDescriptor and
     * the metadata location to keep, or null when the operator chose none
     */
    static async read(request) {
        if (request.xml || request.file) {
            const text = request.xml || await request.file.text();
            const providers = PalantirSamlMetadataNames.serviceProviders(PalantirSamlMetadataNames.parse(text));
            if (providers.length === 0) {
                throw new Error("The metadata holds no SAML2 service provider.");
            }
            let entity = providers[0];
            if (providers.length > 1) {
                const chosen = await PalantirSamlMetadataImport.choose(providers.map(provider => provider.getAttribute("entityID")));
                entity = providers.find(provider => provider.getAttribute("entityID") === chosen);
            }
            return entity ? {entity: entity, metadataLocation: request.url || null} : null;
        }
        if (!PalantirSamlMetadataImport.canReadUrls()) {
            throw new Error("Reading metadata from a URL needs the SAML2 metadata cache endpoint. Paste or choose the metadata instead.");
        }
        let preview = await PalantirSamlMetadataImport.preview(request.url);
        if (!preview.metadata) {
            const chosen = await PalantirSamlMetadataImport.choose(preview.entities ?? []);
            if (!chosen) {
                return null;
            }
            preview = await PalantirSamlMetadataImport.preview(request.url, chosen);
        }
        const entity = PalantirSamlMetadataNames.serviceProviders(PalantirSamlMetadataNames.parse(preview.metadata))[0];
        if (!entity) {
            throw new Error("The metadata holds no SAML2 service provider.");
        }
        return {entity: entity, metadataLocation: preview.metadataLocation};
    }

    /**
     * Ask the server to read the metadata at a URL.
     *
     * @param url the metadata URL
     * @param entityId the service provider to read from an aggregate, if any
     * @returns {Promise<object>} the service providers the metadata holds and the metadata of the one read, if any
     */
    static async preview(url, entityId = null) {
        PalantirDialogs.wait("Reading Metadata", url);
        try {
            return await PalantirSamlMetadataImport.request(url, entityId);
        } finally {
            Swal.close();
        }
    }

    /**
     * Ask the server to read the metadata at a URL, without showing anything.
     *
     * @param url the metadata URL
     * @param entityId the service provider to read from an aggregate, if any
     * @returns {Promise<object>} the service providers the metadata holds and the metadata of the one read, if any
     */
    static async request(url, entityId = null) {
        try {
            return await $.ajax({
                url: `${CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache()}/preview`,
                method: "GET",
                data: entityId ? {metadataLocation: url, entityId: entityId} : {metadataLocation: url},
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
        } catch (xhr) {
            throw new Error(xhr?.responseJSON?.error ?? `CAS could not read the metadata at ${url}.`, {cause: xhr});
        }
    }

    /**
     * Ask which service provider of an aggregate to use.
     *
     * @param entityIds the entity ids of the service providers
     * @returns {Promise<string|null>} the chosen entity id, or null
     */
    static async choose(entityIds) {
        const listId = "samlMetadataImportEntities";
        const result = await Swal.fire({
            title: "Choose the Service Provider",
            text: `The metadata holds ${entityIds.length} service providers.`,
            input: "text",
            inputLabel: "Entity ID",
            inputPlaceholder: "Start typing an entity id",
            showCancelButton: true,
            confirmButtonText: "Use This Service Provider",
            didOpen: () => {
                const datalist = $("<datalist>", {id: listId});
                entityIds.forEach(entityId => datalist.append($("<option>").attr("value", entityId)));
                $(Swal.getPopup()).append(datalist);
                $(Swal.getInput()).attr("list", listId);
            },
            inputValidator: value => entityIds.includes(String(value ?? "").trim())
                ? null
                : "Choose one of the entity ids the metadata holds."
        });
        return result.isConfirmed ? String(result.value).trim() : null;
    }

    /**
     * Whether a value is a web address the wizard may link to.
     *
     * @param value the value
     * @returns {boolean} true for an http or https URL
     */
    static isWebAddress(value) {
        return /^https?:\/\/\S+$/i.test(String(value ?? ""));
    }

    /**
     * Describe a service provider as the parts of an application definition its metadata settles.
     *
     * @param entity the provider's EntityDescriptor
     * @param metadataLocation the metadata location to keep, if any
     * @returns {{definition: object, filled: string[]}} the definition's parts and the names of the fields they fill
     */
    static describe(entity, metadataLocation) {
        const ns = PalantirSamlMetadataNames.NAMESPACES;
        const children = (parent, namespace, name) => Array.from(parent?.children ?? [])
            .filter(child => child.namespaceURI === namespace && child.localName === name);
        const provider = entity.getElementsByTagNameNS(ns.md, "SPSSODescriptor")[0];
        const uiInfo = provider.getElementsByTagNameNS(ns.mdui, "UIInfo")[0] ?? null;
        const ui = name => uiInfo ? Array.from(uiInfo.getElementsByTagNameNS(ns.mdui, name)) : [];
        const definition = {};
        const filled = [];
        const set = (field, label, value) => {
            if (value !== null && value !== undefined && value !== "") {
                definition[field] = value;
                filled.push(label);
            }
        };

        const entityId = entity.getAttribute("entityID");
        set("serviceId", "Entity ID", entityId);
        set("name", "Name", PalantirSamlMetadataNames.nameOf(entity));
        const description = PalantirSamlMetadataNames.localized(ui("Description"));
        set("description", "Description", description ? escapePalantirMarkup(description) : "");
        const informationUrl = PalantirSamlMetadataNames.localized(ui("InformationURL"));
        set("informationUrl", "Information URL", PalantirSamlMetadataImport.isWebAddress(informationUrl) ? informationUrl : "");
        const privacyUrl = PalantirSamlMetadataNames.localized(ui("PrivacyStatementURL"));
        set("privacyUrl", "Privacy URL", PalantirSamlMetadataImport.isWebAddress(privacyUrl) ? privacyUrl : "");
        const logo = ui("Logo")
            .filter(element => PalantirSamlMetadataImport.isWebAddress(element.textContent.trim()))
            .sort((first, second) => (Number(second.getAttribute("width")) || 0) * (Number(second.getAttribute("height")) || 0)
                - (Number(first.getAttribute("width")) || 0) * (Number(first.getAttribute("height")) || 0))[0];
        set("logo", "Logo", logo ? logo.textContent.trim() : "");
        set("metadataLocation", "Metadata Location", metadataLocation ?? "");

        const formats = children(provider, ns.md, "NameIDFormat").map(element => element.textContent.trim()).filter(Boolean);
        set("requiredNameIdFormat", "Required NameID Format", formats.length === 1 ? formats[0] : "");
        set("signAssertions", "Sign Assertions", provider.getAttribute("WantAssertionsSigned") === "true" ? "TRUE" : "");

        const categories = Array.from(entity.getElementsByTagNameNS(ns.mdattr, "EntityAttributes"))
            .flatMap(attributes => Array.from(attributes.getElementsByTagNameNS(ns.saml, "Attribute")))
            .filter(attribute => attribute.getAttribute("Name") === PalantirSamlMetadataImport.ENTITY_CATEGORY)
            .flatMap(attribute => Array.from(attribute.getElementsByTagNameNS(ns.saml, "AttributeValue")))
            .map(value => value.textContent.trim());
        const categoryPolicy = categories.map(category => PalantirSamlMetadataImport.CATEGORY_POLICIES[category]).find(Boolean);
        const requested = provider.getElementsByTagNameNS(ns.md, "RequestedAttribute").length > 0;
        const policy = categoryPolicy ?? (requested ? PalantirSamlMetadataImport.REQUESTED_ATTRIBUTES_POLICY : null);
        set("attributeReleasePolicy", "Attribute Release Policy", policy ? {"@class": policy} : null);

        const contacts = [...children(entity, ns.md, "ContactPerson"), ...children(provider, ns.md, "ContactPerson")]
            .map(person => {
                const text = name => children(person, ns.md, name).map(element => element.textContent.trim()).find(Boolean) ?? "";
                const contact = {"@class": "org.apereo.cas.services.DefaultRegisteredServiceContact"};
                const name = [text("GivenName"), text("SurName")].filter(Boolean).join(" ");
                const details = {
                    name: name,
                    email: text("EmailAddress").replace(/^mailto:/i, ""),
                    phone: text("TelephoneNumber"),
                    department: text("Company"),
                    type: person.getAttribute("contactType") ?? ""
                };
                Object.entries(details).filter(([, value]) => value).forEach(([key, value]) => {
                    contact[key] = value;
                });
                return contact;
            })
            .filter(contact => Object.keys(contact).length > 1);
        set("contacts", "Contacts", contacts.length > 0 ? ["java.util.ArrayList", contacts] : null);
        return {definition: definition, filled: filled};
    }

    /**
     * Merge what the metadata settles over the definition the wizard holds, fill the wizard with the result and say
     * what was filled.
     *
     * @param entity the provider's EntityDescriptor
     * @param metadataLocation the metadata location to keep, if any
     * @returns {Promise<void>} resolved once the wizard is filled
     */
    static async fill(entity, metadataLocation) {
        const {definition, filled} = PalantirSamlMetadataImport.describe(entity, metadataLocation);
        let current;
        try {
            current = palantirParseJson(ace.edit("wizardServiceEditor").getValue() || "{}");
        } catch (error) {
            current = {};
        }
        const merged = {...current, ...definition};
        merged["@class"] = current["@class"] ?? "org.apereo.cas.support.saml.services.SamlRegisteredService";
        populateWizardFromServiceDefinition(merged);
        for (let waited = 0; waited < 50 && (typeof isPopulatingWizard === "undefined" || isPopulatingWizard); waited++) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }
        const location = merged.metadataLocation ? "" : " Enter where CAS should read its metadata from as the metadata location.";
        PalantirDialogs.done("Filled from Metadata", `${definition.serviceId}: ${filled.join(", ")}. Review the fields before saving.${location}`);
    }
}

/**
 * The health of the metadata of a registered SAML2 service provider, checked on demand from the applications' menu.
 *
 * The metadata is downloaded through the server, as the wizard reads it, when its location is a web address or a
 * metadata query location and the service id names one entity: the download, whether the service provider is in it,
 * and the expiry of the certificates it publishes are reported, and the downloaded metadata is shown. The metadata
 * is also resolved the way CAS resolves it at login, through the SAML2 metadata cache with its signature and validity
 * checks, whatever the location is; that resolution and the expiry of the metadata are reported too. Nothing is
 * registered, stored or removed from the cache.
 */
class PalantirSamlMetadataHealth {
    static WARNING_DAYS = 30;

    static STATUS = {
        ok: {icon: "mdi-check-circle", label: "Passed"},
        info: {icon: "mdi-information", label: "Note"},
        skip: {icon: "mdi-minus-circle-outline", label: "Skipped"},
        warn: {icon: "mdi-alert", label: "Warning"},
        fail: {icon: "mdi-close-circle", label: "Failed"}
    };

    static serviceId = null;

    /**
     * Whether the metadata health of an application can be checked: it is a SAML2 service provider and the SAML2
     * metadata cache endpoint is available.
     *
     * @param service the registered service
     * @returns {boolean} true when the check applies
     */
    static applies(service) {
        return String(service?.["@class"] ?? "").includes("SamlRegisteredService")
            && Boolean(CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache());
    }

    /**
     * Whether a service id is a pattern rather than one entity id. Dots are allowed in an entity id, so only the other
     * characters that make a regular expression count.
     *
     * @param serviceId the service id
     * @returns {boolean} true for a pattern
     */
    static isPattern(serviceId) {
        return /[\^$*+?()[\]{}|\\]/.test(String(serviceId ?? ""));
    }

    /**
     * The service id as a test of entity ids: the whole entity id must match, as CAS matches it.
     *
     * @param serviceId the service id
     * @returns {{test: function(string): boolean}} the test
     */
    static entityMatcher(serviceId) {
        const value = String(serviceId ?? "");
        try {
            const pattern = new RegExp(`^(?:${value})$`);
            return {test: entityId => pattern.test(entityId)};
        } catch (error) {
            return {test: entityId => entityId === value};
        }
    }

    /**
     * The web address to download the metadata from: the metadata location itself when it is one, or, for a metadata
     * query location, the address of the one entity the service id names.
     *
     * @param service the registered service
     * @returns {string|null} the address, or null when the metadata cannot be downloaded from here
     */
    static downloadLocation(service) {
        const location = String(service.metadataLocation ?? "").trim();
        if (/\/entities\/\{0\}$/.test(location)) {
            return PalantirSamlMetadataHealth.isPattern(service.serviceId)
                ? null
                : location.replace("{0}", encodeURIComponent(String(service.serviceId ?? "")));
        }
        return /^https?:\/\//i.test(location) ? location : null;
    }

    /**
     * Read a date the server sent as text, as seconds or milliseconds since the epoch, or as an array of its fields.
     *
     * @param value the date
     * @returns {Date|null} the date, or null when there is none
     */
    static date(value) {
        if (value === undefined || value === null || value === "") {
            return null;
        }
        if (Array.isArray(value)) {
            const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value;
            return new Date(Date.UTC(year, month - 1, day, hour, minute, second));
        }
        const number = Number(value);
        const date = Number.isFinite(number) ? new Date(number < 1e11 ? number * 1000 : number) : new Date(String(value));
        return Number.isNaN(date.getTime()) ? null : date;
    }

    /**
     * The end of the validity of a base64 X.509 certificate, read from its DER encoding: the certificate holds the
     * signed part, which holds an optional version, the serial number, the signature algorithm, the issuer and then
     * the validity, whose second date is the end. UTC times carry two digits of the year, before 2050 from 1950 on.
     * Anything else, such as a value that is not a certificate, reads as no date.
     *
     * @param base64 the certificate
     * @returns {Date|null} the end of its validity, or null when it cannot be read
     */
    static certificateExpiry(base64) {
        try {
            const bytes = Uint8Array.from(atob(String(base64).replace(/\s+/g, "")), character => character.charCodeAt(0));
            const read = offset => {
                let length = bytes[offset + 1];
                let start = offset + 2;
                if (length & 0x80) {
                    const count = length & 0x7f;
                    length = 0;
                    for (let index = 0; index < count; index++) {
                        length = length * 256 + bytes[start + index];
                    }
                    start += count;
                }
                return {tag: bytes[offset], start: start, end: start + length};
            };
            const signed = read(read(0).start);
            const first = read(signed.start);
            const serial = first.tag === 0xa0 ? read(first.end) : first;
            const issuer = read(read(serial.end).end);
            const validity = read(issuer.end);
            const end = read(read(validity.start).end);
            const text = new TextDecoder().decode(bytes.slice(end.start, end.end));
            const digits = end.tag === 0x17 ? `${Number(text.slice(0, 2)) < 50 ? "20" : "19"}${text}` : text;
            if (bytes[0] !== 0x30 || (end.tag !== 0x17 && end.tag !== 0x18) || !/^\d{12}/.test(digits)) {
                return null;
            }
            const date = new Date(Date.UTC(Number(digits.slice(0, 4)), Number(digits.slice(4, 6)) - 1, Number(digits.slice(6, 8)),
                Number(digits.slice(8, 10)), Number(digits.slice(10, 12)), Number(digits.slice(12, 14)) || 0));
            return Number.isNaN(date.getTime()) ? null : date;
        } catch (error) {
            return null;
        }
    }

    /**
     * The certificates a service provider publishes, with what they are used for.
     *
     * @param entity the provider's EntityDescriptor
     * @returns {{use: string, expiry: Date|null}[]} the certificates
     */
    static certificates(entity) {
        const ns = PalantirSamlMetadataNames.NAMESPACES;
        return Array.from(entity.getElementsByTagNameNS(ns.md, "KeyDescriptor")).flatMap(descriptor =>
            Array.from(descriptor.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "X509Certificate"))
                .map(certificate => ({
                    use: descriptor.getAttribute("use") || "signing and encryption",
                    expiry: PalantirSamlMetadataHealth.certificateExpiry(certificate.textContent)
                })));
    }

    /**
     * Judge a date by which something stops being valid: failed once it passed, a warning within the warning period.
     *
     * @param title the title of the check
     * @param expiry the date
     * @param what what expires, to start the sentence
     * @returns {{status: string, title: string, detail: string}} the check
     */
    static expiry(title, expiry, what) {
        const days = (expiry.getTime() - Date.now()) / 86400000;
        const when = expiry.toLocaleString();
        if (days < 0) {
            return {status: "fail", title: title, detail: `${what} expired on ${when}.`};
        }
        if (days < PalantirSamlMetadataHealth.WARNING_DAYS) {
            return {status: "warn", title: title, detail: `${what} expires on ${when}, in ${Math.ceil(days)} day(s).`};
        }
        return {status: "ok", title: title, detail: `${what} is valid until ${when}.`};
    }

    /**
     * Ask CAS to resolve the metadata of an application through its metadata cache.
     *
     * @param service the registered service
     * @param includeMetadata whether to return the metadata of each entity as well
     * @returns {Promise<object>} the resolved entities by entity id, with when they were cached and whether they are valid
     */
    static async resolve(service, includeMetadata) {
        const data = {serviceId: String(service.id), includeMetadata: includeMetadata};
        if (!PalantirSamlMetadataHealth.isPattern(service.serviceId)) {
            data.entityId = service.serviceId;
        }
        try {
            return await $.ajax({
                url: CasActuatorEndpoints.samlIdpRegisteredServiceMetadataCache(),
                method: "GET",
                data: data,
                dataType: "json",
                headers: {"Accept": "application/json"}
            });
        } catch (xhr) {
            throw new Error(xhr?.responseJSON?.error ?? `CAS could not resolve the metadata (HTTP ${xhr?.status}).`, {cause: xhr});
        }
    }

    /**
     * Check the metadata of an application: its location, its download, the service provider in it, the resolution
     * by CAS, the expiry of the metadata and of the certificates the service provider publishes. When the metadata
     * cannot be downloaded from here, the metadata CAS resolved is read for the certificates and shown instead.
     *
     * @param service the registered service
     * @returns {Promise<{checks: object[], metadata: string|null}>} the checks and the downloaded metadata, if any
     */
    static async check(service) {
        const checks = [];
        const location = String(service.metadataLocation ?? "").trim();
        const matcher = PalantirSamlMetadataHealth.entityMatcher(service.serviceId);
        checks.push(location
            ? {status: "ok", title: "Metadata Location", detail: location}
            : {status: "fail", title: "Metadata Location", detail: "The application has no metadata location."});

        let entity = null;
        let metadata = null;
        const url = PalantirSamlMetadataHealth.downloadLocation(service);
        if (url) {
            try {
                const started = performance.now();
                let preview = await PalantirSamlMetadataImport.request(url);
                const entities = preview.entities ?? [];
                checks.push({
                    status: "ok",
                    title: "Download",
                    detail: `Read ${url} in ${Math.round(performance.now() - started)} ms; it holds ${entities.length} service provider(s).`
                });
                const matching = entities.filter(entityId => matcher.test(entityId));
                if (matching.length === 0) {
                    checks.push({status: "fail", title: "Service Provider", detail: `No service provider in the metadata matches ${service.serviceId}.`});
                } else {
                    checks.push({
                        status: "ok",
                        title: "Service Provider",
                        detail: matching.length === 1
                            ? `${matching[0]} is in the metadata.`
                            : `${matching.length} service providers in the metadata match ${service.serviceId}; ${matching[0]} is checked.`
                    });
                    if (preview.entityId !== matching[0]) {
                        preview = await PalantirSamlMetadataImport.request(url, matching[0]);
                    }
                    metadata = preview.metadata ?? null;
                    entity = metadata ? PalantirSamlMetadataNames.serviceProviders(PalantirSamlMetadataNames.parse(metadata))[0] ?? null : null;
                }
            } catch (error) {
                checks.push({status: "fail", title: "Download", detail: error.message});
            }
        } else if (location) {
            checks.push({
                status: "skip",
                title: "Download",
                detail: /\/entities\/\{0\}$/.test(location)
                    ? "A metadata query location is read one entity at a time, and the service id is a pattern rather than one entity id."
                    : "The metadata location is not a web address; CAS reads it itself, as the resolution below shows."
            });
        }

        let resolved = [];
        try {
            const response = await PalantirSamlMetadataHealth.resolve(service, !entity);
            resolved = Object.entries(response ?? {}).filter(([key, value]) => value !== null && typeof value === "object" && matcher.test(key));
            const invalid = resolved.filter(([, details]) => details.valid === false);
            const cached = PalantirSamlMetadataHealth.date(resolved[0]?.[1]?.cachedInstant);
            checks.push(resolved.length === 0
                ? {status: "fail", title: "Resolution by CAS", detail: `CAS resolved the metadata but found no service provider matching ${service.serviceId}.`}
                : {
                    status: invalid.length > 0 ? "fail" : "ok",
                    title: "Resolution by CAS",
                    detail: invalid.length > 0
                        ? `CAS resolved the metadata, but holds ${invalid.map(([entityId]) => entityId).join(", ")} as no longer valid.`
                        : `CAS resolved ${resolved.length === 1 ? resolved[0][0] : `${resolved.length} matching service providers`}`
                            + `${cached ? `, cached on ${cached.toLocaleString()}` : ""}.`
                });
            if (!entity && resolved[0]?.[1]?.metadata) {
                metadata = resolved[0][1].metadata;
                entity = PalantirSamlMetadataNames.serviceProviders(PalantirSamlMetadataNames.parse(metadata))[0] ?? null;
            }
        } catch (error) {
            checks.push({status: "fail", title: "Resolution by CAS", detail: error.message});
        }

        const validUntil = PalantirSamlMetadataHealth.date(resolved[0]?.[1]?.validUntil ?? entity?.getAttribute("validUntil"));
        if (validUntil) {
            checks.push(PalantirSamlMetadataHealth.expiry("Metadata validity", validUntil, "The metadata"));
        } else if (resolved.length > 0 || entity) {
            checks.push({status: "info", title: "Metadata Validity", detail: "The metadata declares no end to its validity."});
        }

        if (entity) {
            const certificates = PalantirSamlMetadataHealth.certificates(entity);
            if (certificates.length === 0) {
                checks.push({status: "info", title: "Certificates", detail: "The service provider publishes no certificates."});
            }
            certificates.forEach(certificate => checks.push(certificate.expiry
                ? PalantirSamlMetadataHealth.expiry(`Certificate for ${certificate.use}`, certificate.expiry, "The certificate")
                : {status: "warn", title: `Certificate for ${certificate.use}`, detail: "The certificate could not be read."}));
        } else {
            checks.push({status: "skip", title: "Certificates", detail: "Certificates are read from the metadata, which could not be read."});
        }
        return {checks: checks, metadata: metadata};
    }

    /**
     * The overall status of a set of checks: failing when one failed, needing attention when one warns, else healthy.
     *
     * @param checks the checks
     * @returns {{status: string, label: string}} the status and how it reads
     */
    static overall(checks) {
        if (checks.some(check => check.status === "fail")) {
            return {status: "fail", label: "Failing"};
        }
        if (checks.some(check => check.status === "warn")) {
            return {status: "warn", label: "Needs Attention"};
        }
        return {status: "ok", label: "Healthy"};
    }

    /**
     * Build the dialog once, with Check again and Close in its footer.
     *
     * @returns {jQuery} the dialog element
     */
    static dialog() {
        const element = $("#samlMetadataHealthDialog");
        if (!element.hasClass("ui-dialog-content")) {
            element.removeClass("hide").dialog({
                autoOpen: false,
                modal: true,
                closeOnEscape: true,
                draggable: false,
                resizable: false,
                width: Math.min(880, $(window).width() - 48),
                maxHeight: Math.max(320, $(window).height() - 48),
                classes: {"ui-dialog": "ui-corner-all palantir-metadata-health-dialog"},
                buttons: [
                    {
                        text: "Check Again",
                        "class": "palantir-metadata-health-again",
                        click: () => {
                            const service = registeredServicesById.get(String(PalantirSamlMetadataHealth.serviceId));
                            if (service) {
                                PalantirSamlMetadataHealth.run(service);
                            }
                        }
                    },
                    {
                        text: "Close",
                        click: () => element.dialog("close")
                    }
                ],
                close: () => {
                    PalantirSamlMetadataHealth.serviceId = null;
                }
            });
        }
        return element;
    }

    /**
     * Open the metadata health of an application and check it.
     *
     * @param serviceId the numeric id of the registered service
     * @returns {Promise<void>} resolved once the checks are shown
     */
    static async open(serviceId) {
        const service = registeredServicesById.get(String(serviceId));
        if (!service) {
            return;
        }
        const element = PalantirSamlMetadataHealth.dialog();
        element.dialog("option", "title", `Metadata health of ${service.name ?? serviceId}`);
        element.dialog("open");
        await PalantirSamlMetadataHealth.run(service);
    }

    /**
     * Check an application and show the result, unless the dialog moved on to another application meanwhile.
     *
     * @param service the registered service
     * @returns {Promise<void>} resolved once the checks are shown
     */
    static async run(service) {
        const serviceId = String(service.id);
        PalantirSamlMetadataHealth.serviceId = serviceId;
        $(".palantir-metadata-health-again").button("option", "disabled", true);
        $("#samlMetadataHealthSummary").attr("data-status", "pending")
            .html(`<span class="palantir-metadata-health__status">Checking</span> ${escapePalantirMarkup(service.serviceId)}`);
        $("#samlMetadataHealthChecks").empty();
        $("#samlMetadataHealthMetadata").prop("hidden", true);
        const report = await PalantirSamlMetadataHealth.check(service);
        if (PalantirSamlMetadataHealth.serviceId !== serviceId) {
            return;
        }
        PalantirSamlMetadataHealth.render(service, report);
        $(".palantir-metadata-health-again").button("option", "disabled", false);
    }

    /**
     * Show the overall status, each check and the downloaded metadata, and center the dialog on its new height.
     *
     * @param service the registered service
     * @param report the checks and the downloaded metadata
     */
    static render(service, report) {
        const overall = PalantirSamlMetadataHealth.overall(report.checks);
        $("#samlMetadataHealthSummary").attr("data-status", overall.status)
            .html(`<span class="palantir-metadata-health__status">${overall.label}</span> ${escapePalantirMarkup(service.serviceId)}`);
        $("#samlMetadataHealthChecks").html(report.checks.map(check => {
            const status = PalantirSamlMetadataHealth.STATUS[check.status];
            return `
                <li class="palantir-metadata-health__check is-${check.status}" data-check-status="${check.status}"
                    data-check-title="${escapePalantirMarkup(check.title)}">
                    <i class="mdi ${status.icon}" aria-hidden="true"></i>
                    <div>
                        <strong>${escapePalantirMarkup(check.title)}<span class="palantir-sr-only">: ${status.label}</span></strong>
                        <span>${escapePalantirMarkup(check.detail)}</span>
                    </div>
                </li>`;
        }).join(""));
        $("#samlMetadataHealthMetadata").prop("hidden", !report.metadata);
        if (report.metadata) {
            const editor = initializeAceEditor("samlMetadataHealthEditor", "xml", {useWorker: false});
            editor.setValue(report.metadata);
            editor.setReadOnly(true);
            editor.gotoLine(1);
        }
        $("#samlMetadataHealthDialog").dialog("option", "position", {my: "center", at: "center", of: window});
    }
}
