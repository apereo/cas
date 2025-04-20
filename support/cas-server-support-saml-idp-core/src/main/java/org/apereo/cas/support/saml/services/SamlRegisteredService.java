package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
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

    @JacksonInject("logoutResponseEnabled")
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
    @JacksonInject("signAssertions")
    private TriStateBoolean signAssertions = TriStateBoolean.FALSE;

    @JacksonInject("signUnsolicitedAuthnRequest")
    private boolean signUnsolicitedAuthnRequest;

    @JacksonInject("skipGeneratingAssertionNameId")
    private boolean skipGeneratingAssertionNameId;

    @JacksonInject("skipGeneratingSubjectConfirmationInResponseTo")
    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    @JacksonInject("isSkipGeneratingResponseInResponseTo")
    private boolean isSkipGeneratingResponseInResponseTo;

    @JacksonInject("skipGeneratingSubjectConfirmationNotOnOrAfter")
    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    @JacksonInject("skipGeneratingSubjectConfirmationRecipient")
    private boolean skipGeneratingSubjectConfirmationRecipient;

    @JacksonInject("skipGeneratingSubjectConfirmationAddress")
    private boolean skipGeneratingSubjectConfirmationAddress;

    @JacksonInject("skipGeneratingSubjectConfirmationNotBefore")
    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    @JacksonInject("skipGeneratingSubjectConfirmationNameId")
    private boolean skipGeneratingSubjectConfirmationNameId = true;

    @JacksonInject("skipGeneratingNameIdQualifiers")
    private boolean skipGeneratingNameIdQualifiers;

    @JacksonInject("skipGeneratingTransientNameId")
    private boolean skipGeneratingTransientNameId;

    @JacksonInject("skipValidatingAuthnRequest")
    private boolean skipValidatingAuthnRequest;

    @JacksonInject("skipGeneratingServiceProviderNameIdQualifier")
    private boolean skipGeneratingServiceProviderNameIdQualifier;

    @JacksonInject("skipGeneratingAuthenticatingAuthority")
    private boolean skipGeneratingAuthenticatingAuthority;

    @JacksonInject("skipGeneratingNameIdQualifier")
    private boolean skipGeneratingNameIdQualifier;

    @JacksonInject("skipGeneratingSessionNotOnOrAfter")
    private boolean skipGeneratingSessionNotOnOrAfter;
    
    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject("signResponses")
    private TriStateBoolean signResponses = TriStateBoolean.TRUE;

    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject("signLogoutResponse")
    private TriStateBoolean signLogoutResponse = TriStateBoolean.UNDEFINED;

    @JsonDeserialize(using = TriStateBoolean.Deserializer.class)
    @JacksonInject("signLogoutRequest")
    private TriStateBoolean signLogoutRequest = TriStateBoolean.UNDEFINED;

    @JacksonInject("encryptAssertions")
    private boolean encryptAssertions;

    @JacksonInject("encryptAttributes")
    private boolean encryptAttributes;

    @JacksonInject("encryptionOptional")
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

    private Set<String> encryptableAttributes = new HashSet<>(0);

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
