package org.apereo.cas.ws.idp.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
    private String tokenType;
    private long lifetime;
    private String role = WSFederationRegisteredService.class.getSimpleName();
    private String wsdlLocation;
    private String namespace;
    private String addressingNamespace;
    private String wsdlService;
    private String wsdlEndpoint;
    private String appliesTo;
    private boolean use200502Namespace;

    @Override
    protected AbstractRegisteredService newInstance() {
        return new WSFederationRegisteredService();
    }

    public String getAddressingNamespace() {
        return addressingNamespace;
    }

    public void setAddressingNamespace(final String addressingNamespace) {
        this.addressingNamespace = addressingNamespace;
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
                .append(this.addressingNamespace, rhs.addressingNamespace)
                .append(this.protocol, rhs.protocol)
                .append(this.tokenType, rhs.tokenType)
                .append(this.lifetime, rhs.lifetime)
                .append(this.role, rhs.role)
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
                .append(addressingNamespace)
                .append(protocol)
                .append(tokenType)
                .append(lifetime)
                .append(role)
                .append(wsdlLocation)
                .append(namespace)
                .append(wsdlService)
                .append(wsdlEndpoint)
                .append(appliesTo)
                .append(use200502Namespace)
                .toHashCode();
    }
}
