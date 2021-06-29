package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.util.ArrayList;
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
public class SamlRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 1218757374062931021L;

    @ExpressionLanguageCapable
    private String metadataLocation;

    private String metadataProxyLocation;

    /**
     * Defines a filter that requires the presence of a validUntil
     * attribute on the root element of the metadata document.
     * A maximum validity interval of less than 1 means that
     * no restriction is placed on the metadata's validUntil attribute.
     */
    private long metadataMaxValidity;

    private String requiredAuthenticationContextClass;

    private String metadataCriteriaDirection;

    private String metadataCriteriaPattern;

    private String requiredNameIdFormat;

    @ExpressionLanguageCapable
    private String metadataSignatureLocation;

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

    private boolean signAssertions;

    private boolean signUnsolicitedAuthnRequest;

    private boolean skipGeneratingAssertionNameId;

    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    private boolean skipGeneratingSubjectConfirmationRecipient;

    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    private boolean skipGeneratingSubjectConfirmationNameId = true;

    private boolean skipGeneratingNameIdQualifiers;

    private boolean skipGeneratingTransientNameId;

    private boolean skipValidatingAuthnRequest;

    private boolean signResponses = true;

    private boolean encryptAssertions;

    private boolean encryptAttributes;

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

    private List<String> signingSignatureReferenceDigestMethods = new ArrayList<>(0);

    private List<String> signingSignatureAlgorithms = new ArrayList<>(0);

    private List<String> signingSignatureBlackListedAlgorithms = new ArrayList<>(0);

    private List<String> signingSignatureWhiteListedAlgorithms = new ArrayList<>(0);

    private String signingSignatureCanonicalizationAlgorithm;

    private List<String> encryptionDataAlgorithms = new ArrayList<>(0);

    private List<String> encryptionKeyAlgorithms = new ArrayList<>(0);

    private List<String> encryptionBlackListedAlgorithms = new ArrayList<>(0);

    private List<String> encryptionWhiteListedAlgorithms = new ArrayList<>(0);

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "SAML2 Service Provider";
    }

    @Override
    @JsonIgnore
    public int getEvaluationPriority() {
        return 0;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }
}
