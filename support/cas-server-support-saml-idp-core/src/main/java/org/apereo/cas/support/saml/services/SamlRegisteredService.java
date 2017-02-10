package org.apereo.cas.support.saml.services;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue("saml")
public class SamlRegisteredService extends RegexRegisteredService {
    private static final long serialVersionUID = 1218757374062931021L;

    private String metadataLocation;
    
    /**
     * Defines a filter that requires the presence of a validUntil
     * attribute on the root element of the metadata document.
     * A maximum validity interval of less than 1 means that
     * no restriction is placed on the metadata's validUntil attribute.
     */
    @Column(updatable = true, insertable = true)
    private long metadataMaxValidity;

    @Column(updatable = true, insertable = true)
    private String requiredAuthenticationContextClass;

    @Column(updatable = true, insertable = true)
    private String metadataCriteriaDirection;

    @Column(updatable = true, insertable = true)
    private String metadataCriteriaPattern;
    
    @Column(updatable = true, insertable = true)
    private String requiredNameIdFormat;

    @Column(updatable = true, insertable = true)
    private String metadataSignatureLocation;

    @Column(updatable = true, insertable = true)
    private boolean signAssertions;

    @Column(updatable = true, insertable = true)
    private boolean signResponses = true;

    @Column(updatable = true, insertable = true)
    private boolean encryptAssertions;

    @Column(updatable = true, insertable = true)
    private String metadataCriteriaRoles = "SPSSODescriptor";

    @Column(updatable = true, insertable = true)
    private boolean metadataCriteriaRemoveEmptyEntitiesDescriptors = true;

    @Column(updatable = true, insertable = true)
    private boolean metadataCriteriaRemoveRolelessEntityDescriptors = true;
        
    /**
     * Instantiates a new Saml registered service.
     */
    public SamlRegisteredService() {
        super();
    }

    public void setMetadataLocation(final String metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public String getMetadataLocation() {
        return this.metadataLocation;
    }

    public boolean isSignAssertions() {
        return this.signAssertions;
    }

    public void setSignAssertions(final boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public boolean isSignResponses() {
        return this.signResponses;
    }

    public void setSignResponses(final boolean signResponses) {
        this.signResponses = signResponses;
    }

    public String getRequiredAuthenticationContextClass() {
        return this.requiredAuthenticationContextClass;
    }

    public void setRequiredAuthenticationContextClass(final String requiredAuthenticationContextClass) {
        this.requiredAuthenticationContextClass = requiredAuthenticationContextClass;
    }

    public String getMetadataSignatureLocation() {
        return this.metadataSignatureLocation;
    }

    public void setMetadataSignatureLocation(final String metadataSignatureLocation) {
        this.metadataSignatureLocation = metadataSignatureLocation;
    }

    public boolean isEncryptAssertions() {
        return this.encryptAssertions;
    }

    public void setEncryptAssertions(final boolean encryptAssertions) {
        this.encryptAssertions = encryptAssertions;
    }

    public long getMetadataMaxValidity() {
        return this.metadataMaxValidity;
    }

    public void setMetadataMaxValidity(final long metadataMaxValidity) {
        this.metadataMaxValidity = metadataMaxValidity;
    }

    public String getMetadataCriteriaDirection() {
        return metadataCriteriaDirection;
    }

    public void setMetadataCriteriaDirection(final String metadataCriteriaDirection) {
        this.metadataCriteriaDirection = metadataCriteriaDirection;
    }

    public String getMetadataCriteriaPattern() {
        return metadataCriteriaPattern;
    }

    public void setMetadataCriteriaPattern(final String metadataCriteriaPattern) {
        this.metadataCriteriaPattern = metadataCriteriaPattern;
    }
    
    public String getRequiredNameIdFormat() {
        return requiredNameIdFormat;
    }

    public void setRequiredNameIdFormat(final String requiredNameIdFormat) {
        this.requiredNameIdFormat = requiredNameIdFormat;
    }

    public String getMetadataCriteriaRoles() {
        return metadataCriteriaRoles;
    }

    public void setMetadataCriteriaRoles(final String metadataCriteriaRole) {
        this.metadataCriteriaRoles = metadataCriteriaRole;
    }

    public boolean isMetadataCriteriaRemoveEmptyEntitiesDescriptors() {
        return metadataCriteriaRemoveEmptyEntitiesDescriptors;
    }

    public void setMetadataCriteriaRemoveEmptyEntitiesDescriptors(final boolean metadataCriteriaRemoveEmptyEntitiesDescriptors) {
        this.metadataCriteriaRemoveEmptyEntitiesDescriptors = metadataCriteriaRemoveEmptyEntitiesDescriptors;
    }

    public boolean isMetadataCriteriaRemoveRolelessEntityDescriptors() {
        return metadataCriteriaRemoveRolelessEntityDescriptors;
    }

    public void setMetadataCriteriaRemoveRolelessEntityDescriptors(final boolean metadataCriteriaRemoveRolelessEntityDescriptors) {
        this.metadataCriteriaRemoveRolelessEntityDescriptors = metadataCriteriaRemoveRolelessEntityDescriptors;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) source;
            setMetadataLocation(samlRegisteredService.getMetadataLocation());
            setSignAssertions(samlRegisteredService.isSignAssertions());
            setSignResponses(samlRegisteredService.isSignResponses());
            setRequiredAuthenticationContextClass(samlRegisteredService.getRequiredAuthenticationContextClass());
            setMetadataMaxValidity(samlRegisteredService.getMetadataMaxValidity());
            setMetadataSignatureLocation(samlRegisteredService.getMetadataSignatureLocation());
            setEncryptAssertions(samlRegisteredService.isEncryptAssertions());
            setRequiredNameIdFormat(samlRegisteredService.getRequiredNameIdFormat());

            setMetadataCriteriaDirection(samlRegisteredService.getMetadataCriteriaDirection());
            setMetadataCriteriaPattern(samlRegisteredService.getMetadataCriteriaPattern());

            setMetadataCriteriaRemoveEmptyEntitiesDescriptors(samlRegisteredService.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            setMetadataCriteriaRemoveRolelessEntityDescriptors(samlRegisteredService.isMetadataCriteriaRemoveRolelessEntityDescriptors());
            setMetadataCriteriaRoles(samlRegisteredService.getMetadataCriteriaRoles());
            
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SamlRegisteredService rhs = (SamlRegisteredService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.metadataLocation, rhs.metadataLocation)
                .append(this.metadataMaxValidity, rhs.metadataMaxValidity)
                .append(this.requiredAuthenticationContextClass, rhs.requiredAuthenticationContextClass)
                .append(this.metadataSignatureLocation, rhs.metadataSignatureLocation)
                .append(this.signAssertions, rhs.signAssertions)
                .append(this.signResponses, rhs.signResponses)
                .append(this.encryptAssertions, rhs.encryptAssertions)
                .append(this.requiredNameIdFormat, rhs.requiredNameIdFormat)
                .append(this.metadataCriteriaDirection, rhs.metadataCriteriaDirection)
                .append(this.metadataCriteriaPattern, rhs.metadataCriteriaPattern)
                .append(this.metadataCriteriaRemoveEmptyEntitiesDescriptors, rhs.metadataCriteriaRemoveEmptyEntitiesDescriptors)
                .append(this.metadataCriteriaRemoveRolelessEntityDescriptors, rhs.metadataCriteriaRemoveRolelessEntityDescriptors)
                .append(this.metadataCriteriaRoles, rhs.metadataCriteriaRoles)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.metadataLocation)
                .append(this.metadataMaxValidity)
                .append(this.requiredAuthenticationContextClass)
                .append(this.metadataSignatureLocation)
                .append(this.signAssertions)
                .append(this.signResponses)
                .append(this.encryptAssertions)
                .append(this.requiredNameIdFormat)
                .append(this.metadataCriteriaDirection)
                .append(this.metadataCriteriaPattern)
                .append(this.metadataCriteriaRemoveEmptyEntitiesDescriptors)
                .append(this.metadataCriteriaRemoveRolelessEntityDescriptors)
                .append(this.metadataCriteriaRoles)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("metadataLocation", this.metadataLocation)
                .append("metadataMaxValidity", this.metadataMaxValidity)
                .append("requiredAuthenticationContextClass", this.requiredAuthenticationContextClass)
                .append("metadataSignatureLocation", this.metadataSignatureLocation)
                .append("signAssertions", this.signAssertions)
                .append("signResponses", this.signResponses)
                .append("encryptAssertions", this.encryptAssertions)
                .append("requiredNameIdFormat", this.requiredNameIdFormat)
                .append("metadataCriteriaDirection", this.metadataCriteriaDirection)
                .append("metadataCriteriaPattern", this.metadataCriteriaPattern)
                .append("metadataCriteriaRemoveEmptyEntitiesDescriptors", this.metadataCriteriaRemoveEmptyEntitiesDescriptors)
                .append("metadataCriteriaRemoveRolelessEntityDescriptors", this.metadataCriteriaRemoveRolelessEntityDescriptors)
                .append("metadataCriteriaRoles", this.metadataCriteriaRoles)
                .toString();
    }
}
