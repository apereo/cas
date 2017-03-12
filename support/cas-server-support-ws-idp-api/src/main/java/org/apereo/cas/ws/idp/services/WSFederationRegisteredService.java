package org.apereo.cas.ws.idp.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.ws.idp.DefaultFederationClaim;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link WSFederationRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@DiscriminatorValue("wsfed")
public class WSFederationRegisteredService extends RegexRegisteredService {
    private static final long serialVersionUID = -3700571300568534062L;

    private String realm;
    private String protocol;
    private String displayName;
    private String description;
    private String tokenType;
    private long lifetime;
    private String role;
    private Collection<DefaultFederationClaim> claims = new HashSet<>();
    private String wsdlLocation;
    private String namespace;
    private String wsdlService;
    private String wsdlEndpoint;
    private String appliesTo;
    private boolean use200502Namespace;
    
    @Override
    protected AbstractRegisteredService newInstance() {
        return new WSFederationRegisteredService();
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(final String tokenType) {
        this.tokenType = tokenType;
    }

    public long getLifetime() {
        return lifetime;
    }

    public void setLifetime(final long lifetime) {
        this.lifetime = lifetime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public Collection<DefaultFederationClaim> getClaims() {
        return claims;
    }

    public void setClaims(final Collection<DefaultFederationClaim> claims) {
        this.claims = claims;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public void setWsdlLocation(final String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(final String wsdlService) {
        this.wsdlService = wsdlService;
    }

    public String getWsdlEndpoint() {
        return wsdlEndpoint;
    }

    public void setWsdlEndpoint(final String wsdlEndpoint) {
        this.wsdlEndpoint = wsdlEndpoint;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(final String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public boolean isUse200502Namespace() {
        return use200502Namespace;
    }

    public void setUse200502Namespace(final boolean use200502Namespace) {
        this.use200502Namespace = use200502Namespace;
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
        final WSFederationRegisteredService rhs = (WSFederationRegisteredService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.realm, rhs.realm)
                .append(this.protocol, rhs.protocol)
                .append(this.displayName, rhs.displayName)
                .append(this.description, rhs.description)
                .append(this.tokenType, rhs.tokenType)
                .append(this.lifetime, rhs.lifetime)
                .append(this.role, rhs.role)
                .append(this.claims, rhs.claims)
                .append(this.wsdlLocation, rhs.wsdlLocation)
                .append(this.namespace, rhs.namespace)
                .append(this.wsdlService, rhs.wsdlService)
                .append(this.wsdlEndpoint, rhs.wsdlEndpoint)
                .append(this.appliesTo, rhs.appliesTo)
                .append(this.use200502Namespace, rhs.use200502Namespace)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(realm)
                .append(protocol)
                .append(displayName)
                .append(description)
                .append(tokenType)
                .append(lifetime)
                .append(role)
                .append(claims)
                .append(wsdlLocation)
                .append(namespace)
                .append(wsdlService)
                .append(wsdlEndpoint)
                .append(appliesTo)
                .append(use200502Namespace)
                .toHashCode();
    }
}
