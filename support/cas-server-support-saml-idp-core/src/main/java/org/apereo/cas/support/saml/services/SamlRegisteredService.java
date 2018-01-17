package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.MapKeyColumn;
import java.util.Map;
import java.util.TreeMap;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue("saml")
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SamlRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = 1218757374062931021L;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataLocation;

    /**
     * Defines a filter that requires the presence of a validUntil
     * attribute on the root element of the metadata document.
     * A maximum validity interval of less than 1 means that
     * no restriction is placed on the metadata's validUntil attribute.
     */
    @Column(updatable = true, insertable = true)
    private long metadataMaxValidity;

    @Column(length = 255, updatable = true, insertable = true)
    private String requiredAuthenticationContextClass;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataCriteriaDirection;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataCriteriaPattern;

    @Column(length = 255, updatable = true, insertable = true)
    private String requiredNameIdFormat;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataSignatureLocation;

    @Column(length = 255, updatable = true, insertable = true)
    private String serviceProviderNameIdQualifier;

    @Column(length = 255, updatable = true, insertable = true)
    private String nameIdQualifier;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataExpirationDuration = "PT60M";

    @Column(updatable = true, insertable = true)
    private boolean signAssertions;

    @Column(updatable = true, insertable = true)
    private boolean skipGeneratingAssertionNameId;

    @Column(updatable = true, insertable = true)
    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    @Column(updatable = true, insertable = true)
    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    @Column(updatable = true, insertable = true)
    private boolean skipGeneratingSubjectConfirmationRecipient;

    @Column(updatable = true, insertable = true)
    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    @Column(updatable = true, insertable = true)
    private boolean signResponses = true;

    @Column(updatable = true, insertable = true)
    private boolean encryptAssertions;

    @Column(length = 255, updatable = true, insertable = true)
    private String metadataCriteriaRoles = SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME;

    @Column(updatable = true, insertable = true)
    private boolean metadataCriteriaRemoveEmptyEntitiesDescriptors = true;

    @Column(updatable = true, insertable = true)
    private boolean metadataCriteriaRemoveRolelessEntityDescriptors = true;

    @Column(length = 255, updatable = true, insertable = true)
    private String signingCredentialType;

    @ElementCollection
    @CollectionTable(name = "SamlRegisteredService_AttributeNameFormats")
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> attributeNameFormats = new TreeMap<>();

    @ElementCollection
    @CollectionTable(name = "SamlRegisteredService_AttributeFriendlyNames")
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> attributeFriendlyNames = new TreeMap<>();

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final SamlRegisteredService service = (SamlRegisteredService) source;
            setMetadataLocation(service.getMetadataLocation());
            setSignAssertions(service.isSignAssertions());
            setSignResponses(service.isSignResponses());
            setRequiredAuthenticationContextClass(service.getRequiredAuthenticationContextClass());
            setMetadataMaxValidity(service.getMetadataMaxValidity());
            setMetadataSignatureLocation(service.getMetadataSignatureLocation());
            setEncryptAssertions(service.isEncryptAssertions());
            setRequiredNameIdFormat(service.getRequiredNameIdFormat());
            setMetadataCriteriaDirection(service.getMetadataCriteriaDirection());
            setMetadataCriteriaPattern(service.getMetadataCriteriaPattern());
            setMetadataExpirationDuration(service.metadataExpirationDuration);
            setMetadataCriteriaRemoveEmptyEntitiesDescriptors(service.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            setMetadataCriteriaRemoveRolelessEntityDescriptors(service.isMetadataCriteriaRemoveRolelessEntityDescriptors());
            setMetadataCriteriaRoles(service.getMetadataCriteriaRoles());
            setAttributeNameFormats(service.getAttributeNameFormats());
            setAttributeFriendlyNames(service.getAttributeFriendlyNames());
            setNameIdQualifier(service.getNameIdQualifier());
            setServiceProviderNameIdQualifier(service.serviceProviderNameIdQualifier);
            setSkipGeneratingAssertionNameId(service.isSkipGeneratingAssertionNameId());
            setSkipGeneratingSubjectConfirmationInResponseTo(service.skipGeneratingSubjectConfirmationInResponseTo);
            setSkipGeneratingSubjectConfirmationNotBefore(service.skipGeneratingSubjectConfirmationNotBefore);
            setSkipGeneratingSubjectConfirmationNotOnOrAfter(service.skipGeneratingSubjectConfirmationNotOnOrAfter);
            setSkipGeneratingSubjectConfirmationRecipient(service.skipGeneratingSubjectConfirmationRecipient);
            setSigningCredentialType(service.getSigningCredentialType());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "SAML2 Service Provider";
    }
}
