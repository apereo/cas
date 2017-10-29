package org.apereo.cas.ws.idp.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.ws.idp.WSFederationConstants;

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

    private String realm = WSFederationConstants.REALM_DEFAULT_URI;
    private String protocol = WSFederationConstants.WST_NS_05_12;
    private String tokenType = WSFederationConstants.WSS_SAML2_TOKEN_TYPE;
    private String wsdlLocation;
    private String namespace = WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512;
    private String addressingNamespace = WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING;
    private String policyNamespace;
    private String wsdlService = WSFederationConstants.SECURITY_TOKEN_SERVICE;
    private String wsdlEndpoint = WSFederationConstants.SECURITY_TOKEN_SERVICE_ENDPOINT;
    private String appliesTo;

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
        return StringUtils.defaultIfBlank(appliesTo, this.realm);
    }

    public void setAppliesTo(final String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getPolicyNamespace() {
        return policyNamespace;
    }

    public void setPolicyNamespace(final String policyNamespace) {
        this.policyNamespace = policyNamespace;
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
                .append(this.wsdlLocation, rhs.wsdlLocation)
                .append(this.namespace, rhs.namespace)
                .append(this.policyNamespace, rhs.policyNamespace)
                .append(this.wsdlService, rhs.wsdlService)
                .append(this.wsdlEndpoint, rhs.wsdlEndpoint)
                .append(getAppliesTo(), rhs.getAppliesTo())
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
                .append(wsdlLocation)
                .append(namespace)
                .append(wsdlService)
                .append(wsdlEndpoint)
                .append(getAppliesTo())
                .append(policyNamespace)
                .toHashCode();
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "WS Federation Relying Party";
    }
}
