package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.trusted.TrustedAuthenticationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link WsFederationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class WsFederationProperties {

    private String identityAttribute = "upn";
    private String identityProviderIdentifier = "https://adfs.example.org/adfs/services/trust";
    private String identityProviderUrl = "https://adfs.example.org/adfs/ls/";
    private String signingCertificateResources = "classpath:adfs-signing.crt";
    private String relyingPartyIdentifier = "urn:cas:localhost";
    private int tolerance = 10000;
    private String attributesType = "WSFED";
    private boolean attributeResolverEnabled = true;

    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties principal = new PersonDirPrincipalResolverProperties();

    public PersonDirPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirPrincipalResolverProperties principal) {
        this.principal = principal;
    }
    
    public boolean isAttributeResolverEnabled() {
        return attributeResolverEnabled;
    }

    public void setAttributeResolverEnabled(final boolean attributeResolverEnabled) {
        this.attributeResolverEnabled = attributeResolverEnabled;
    }

    public String getIdentityAttribute() {
        return identityAttribute;
    }

    public void setIdentityAttribute(final String identityAttribute) {
        this.identityAttribute = identityAttribute;
    }

    public String getIdentityProviderIdentifier() {
        return identityProviderIdentifier;
    }

    public void setIdentityProviderIdentifier(final String identityProviderIdentifier) {
        this.identityProviderIdentifier = identityProviderIdentifier;
    }

    public String getIdentityProviderUrl() {
        return identityProviderUrl;
    }

    public void setIdentityProviderUrl(final String identityProviderUrl) {
        this.identityProviderUrl = identityProviderUrl;
    }

    public String getSigningCertificateResources() {
        return signingCertificateResources;
    }

    public void setSigningCertificateResources(final String signingCertificateResources) {
        this.signingCertificateResources = signingCertificateResources;
    }

    public String getRelyingPartyIdentifier() {
        return relyingPartyIdentifier;
    }

    public void setRelyingPartyIdentifier(final String relyingPartyIdentifier) {
        this.relyingPartyIdentifier = relyingPartyIdentifier;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(final int tolerance) {
        this.tolerance = tolerance;
    }

    public String getAttributesType() {
        return attributesType;
    }

    public void setAttributesType(final String attributesType) {
        this.attributesType = attributesType;
    }
}
