function createRegisteredServiceSaml2Fields() {
    createInputField({
        labelTitle: "Metadata Location",
        name: "registeredServiceMetadataLocation",
        paramName: "metadataLocation",
        required: true,
        containerId: "editServiceWizardSamlContainer",
        title: "Define the metadata location (URL, file path, etc) for the SAML2 service provider."
    });

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
        paramName: "signingSignatureBlackListedAlgorithms",
        required: false,
        containerId: "editServiceWizardSamlContainer",
        title: "Defines a comma-separated list of whitelisted signature algorithms to avoid when signing SAML messages."
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
        title: "Defines a comma-separated list of whitelisted algorithms to avoid when encrypting SAML assertions."
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
            value: "org.apereo.cas.support.saml.services.EduPersonTargetedIdAttributeReleasePolicy",
            text: "EDUPERSON TARGETED ID",
            data: {
                markerClass: true,
                serviceClass: "SamlRegisteredService"
            }
        },
        ...(
            scriptFactoryAvailable
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
