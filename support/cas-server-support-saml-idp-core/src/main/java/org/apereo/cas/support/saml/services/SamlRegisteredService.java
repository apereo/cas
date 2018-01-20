package org.apereo.cas.support.saml.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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

    @Column
    private String metadataLocation;

    /**
     * Defines a filter that requires the presence of a validUntil
     * attribute on the root element of the metadata document.
     * A maximum validity interval of less than 1 means that
     * no restriction is placed on the metadata's validUntil attribute.
     */
    @Column
    private long metadataMaxValidity;

    @Column
    private String requiredAuthenticationContextClass;

    @Column
    private String metadataCriteriaDirection;

    @Column
    private String metadataCriteriaPattern;

    @Column
    private String requiredNameIdFormat;

    @Column
    private String metadataSignatureLocation;

    @Column
    private String serviceProviderNameIdQualifier;

    @Column
    private String nameIdQualifier;

    @Column
    private String metadataExpirationDuration = "PT60M";

    @Column
    private boolean signAssertions;

    @Column
    private boolean skipGeneratingAssertionNameId;

    @Column
    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    @Column
    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    @Column
    private boolean skipGeneratingSubjectConfirmationRecipient;

    @Column
    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    @Column
    private boolean signResponses = true;

    @Column
    private boolean encryptAssertions;

    @Column
    private String metadataCriteriaRoles = SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME;

    @Column
    private boolean metadataCriteriaRemoveEmptyEntitiesDescriptors = true;

    @Column
    private boolean metadataCriteriaRemoveRolelessEntityDescriptors = true;

    @Column
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
    @SneakyThrows
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
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
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "SAML2 Service Provider";
    }
}
