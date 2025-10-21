package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SamlRegisteredService extends BaseWebBasedRegisteredService {

    /**
     * Service definition friendly name.
     */
    public static final String FRIENDLY_NAME = "SAML2 Service Provider";

    @Serial
    private static final long serialVersionUID = 1218757374062931021L;

    @ExpressionLanguageCapable
    private String metadataLocation;

    @ExpressionLanguageCapable
    private String idpMetadataLocation;

    private String metadataProxyLocation;

    /**
     * Defines a filter that requires the presence of a validUntil
     * attribute on the root element of the metadata document.
     * A maximum validity interval of less than 1 means that
     * no restriction is placed on the metadata's validUntil attribute.
     */
    private long metadataMaxValidity;

    @ExpressionLanguageCapable
    private String requiredAuthenticationContextClass;

    private String metadataCriteriaDirection = "INCLUDE";

    private String metadataCriteriaPattern;

    private Map<String, List<String>> metadataCriteriaEntityAttributes = new HashMap<>();
    
    private String requiredNameIdFormat;

    @ExpressionLanguageCapable
    private String metadataSignatureLocation;

    @JacksonInject(value = "logoutResponseEnabled", optional = OptBoolean.TRUE, useInput = OptBoolean.TRUE)
    private boolean logoutResponseEnabled = true;

    private String logoutResponseBinding;

    private boolean requireSignedRoot = true;

    private String subjectLocality;

    private String serviceProviderNameIdQualifier;

    private String nameIdQualifier;

    private String metadataExpirationDuration = "PT60M";

    private String signingCredentialFingerprint;

    private String issuerEntityId;

    private String signingKeyAlgorithm;

    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject(value = "signAssertions", optional = OptBoolean.TRUE, useInput = OptBoolean.FALSE)
    private TriStateBoolean signAssertions = TriStateBoolean.FALSE;

    @JacksonInject(value = "signUnsolicitedAuthnRequest", optional = OptBoolean.TRUE)
    private boolean signUnsolicitedAuthnRequest;

    @JacksonInject(value = "skipGeneratingAssertionNameId", optional = OptBoolean.TRUE)
    private boolean skipGeneratingAssertionNameId;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationInResponseTo", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    @JacksonInject(value = "isSkipGeneratingResponseInResponseTo", optional = OptBoolean.TRUE)
    private boolean isSkipGeneratingResponseInResponseTo;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationNotOnOrAfter", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationRecipient", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationRecipient;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationAddress", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationAddress;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationNotBefore", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    @JacksonInject(value = "skipGeneratingSubjectConfirmationNameId", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSubjectConfirmationNameId = true;

    @JacksonInject(value = "skipGeneratingNameIdQualifiers", optional = OptBoolean.TRUE)
    private boolean skipGeneratingNameIdQualifiers;

    @JacksonInject(value = "skipGeneratingTransientNameId", optional = OptBoolean.TRUE)
    private boolean skipGeneratingTransientNameId;

    @JacksonInject(value = "skipValidatingAuthnRequest", optional = OptBoolean.TRUE)
    private boolean skipValidatingAuthnRequest;

    @JacksonInject(value = "skipGeneratingServiceProviderNameIdQualifier", optional = OptBoolean.TRUE)
    private boolean skipGeneratingServiceProviderNameIdQualifier;

    @JacksonInject(value = "skipGeneratingAuthenticatingAuthority", optional = OptBoolean.TRUE)
    private boolean skipGeneratingAuthenticatingAuthority;

    @JacksonInject(value = "skipGeneratingNameIdQualifier", optional = OptBoolean.TRUE)
    private boolean skipGeneratingNameIdQualifier;

    @JacksonInject(value = "skipGeneratingSessionNotOnOrAfter", optional = OptBoolean.TRUE)
    private boolean skipGeneratingSessionNotOnOrAfter;

    private boolean validateMetadataCertificates;
    
    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject(value = "signResponses", optional = OptBoolean.TRUE)
    private TriStateBoolean signResponses = TriStateBoolean.TRUE;

    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject(value = "signLogoutResponse", optional = OptBoolean.TRUE)
    private TriStateBoolean signLogoutResponse = TriStateBoolean.UNDEFINED;

    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject(value = "signLogoutRequest", optional = OptBoolean.TRUE)
    private TriStateBoolean signLogoutRequest = TriStateBoolean.UNDEFINED;

    @JacksonInject(value = "encryptAssertions", optional = OptBoolean.TRUE)
    private boolean encryptAssertions;

    @JacksonInject(value = "encryptAttributes", optional = OptBoolean.TRUE)
    private boolean encryptAttributes;

    @JacksonInject(value = "encryptionOptional", optional = OptBoolean.TRUE)
    private boolean encryptionOptional;

    private String metadataCriteriaRoles = SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME;

    private boolean metadataCriteriaRemoveEmptyEntitiesDescriptors = true;

    private boolean metadataCriteriaRemoveRolelessEntityDescriptors = true;

    private String signingCredentialType;

    private String assertionAudiences;

    private int skewAllowance;

    private String whiteListBlackListPrecedence;

    private Map<String, String> attributeNameFormats = new TreeMap<>();

    private Map<String, String> attributeFriendlyNames = new TreeMap<>();

    private Map<String, String> attributeValueTypes = new TreeMap<>();

    private Set<String> encryptableAttributes = new HashSet<>();

    private List<String> signingSignatureReferenceDigestMethods = new ArrayList<>();

    private List<String> signingSignatureAlgorithms = new ArrayList<>();

    private List<String> signingSignatureBlackListedAlgorithms = new ArrayList<>();

    private List<String> signingSignatureWhiteListedAlgorithms = new ArrayList<>();

    private String signingSignatureCanonicalizationAlgorithm;

    private List<String> encryptionDataAlgorithms = new ArrayList<>();

    private List<String> encryptionKeyAlgorithms = new ArrayList<>();

    private List<String> encryptionBlackListedAlgorithms = new ArrayList<>();

    private List<String> encryptionWhiteListedAlgorithms = new ArrayList<>();

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return FRIENDLY_NAME;
    }

    @Override
    @JsonIgnore
    public int getEvaluationPriority() {
        return 0;
    }
}
