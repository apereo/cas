package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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

    @Column(name = "reqAuthnContextClass")
    private String requiredAuthenticationContextClass;

    @Column
    private String metadataCriteriaDirection;

    @Column
    private String metadataCriteriaPattern;

    @Column
    private String requiredNameIdFormat;

    @Column
    private String metadataSignatureLocation;

    @Column(name = "spNameIdQualifier")
    private String serviceProviderNameIdQualifier;

    @Column
    private String nameIdQualifier;

    @Column
    private String metadataExpirationDuration = "PT60M";

    @Column
    private boolean signAssertions;

    @Column(name = "skipGenAssertionNameId")
    private boolean skipGeneratingAssertionNameId;

    @Column(name = "skipGenSubConfInRespTo")
    private boolean skipGeneratingSubjectConfirmationInResponseTo;

    @Column(name = "skipGenSubConNotOnOrAfter")
    private boolean skipGeneratingSubjectConfirmationNotOnOrAfter;

    @Column(name = "skipGenSubConRecipient")
    private boolean skipGeneratingSubjectConfirmationRecipient;

    @Column(name = "skipGenSubConfNotBefore")
    private boolean skipGeneratingSubjectConfirmationNotBefore = true;

    @Column(name = "skipGenSubConfNameId")
    private boolean skipGeneratingSubjectConfirmationNameId = true;

    @Column
    private boolean signResponses = true;

    @Column
    private boolean encryptAssertions;

    @Column
    private boolean encryptAttributes;

    @Column
    private String metadataCriteriaRoles = SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME;

    @Column(name = "mdCriteriaRmEmptyEntities")
    private boolean metadataCriteriaRemoveEmptyEntitiesDescriptors = true;

    @Column(name = "mdCriteriaRmRolelessEntities")
    private boolean metadataCriteriaRemoveRolelessEntityDescriptors = true;

    @Column
    private String signingCredentialType;

    @Column
    private String assertionAudiences;

    @ElementCollection
    @CollectionTable(name = "SamlRegisteredService_AttributeNameFormats")
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributeNameFormats = new TreeMap<>();

    @ElementCollection
    @CollectionTable(name = "SamlRegisteredService_AttributeFriendlyNames")
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributeFriendlyNames = new TreeMap<>();

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "SAML2 Service Provider";
    }
}
