package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link WsFederationDelegationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class WsFederationDelegationProperties {

    private String identityAttribute = "upn";
    private String identityProviderIdentifier = "https://adfs.example.org/adfs/services/trust";
    private String identityProviderUrl = "https://adfs.example.org/adfs/ls/";
    private String signingCertificateResources = "classpath:adfs-signing.crt";
    private String relyingPartyIdentifier = "urn:cas:localhost";
    private String tolerance = "PT10S";
    private String attributesType = "WSFED";
    private boolean attributeResolverEnabled = true;
    private boolean autoRedirect = true;

    private String encryptionPrivateKey = "classpath:private.key";
    private String encryptionCertificate = "classpath:certificate.crt";
    private String encryptionPrivateKeyPassword = "NONE";

    @NestedConfigurationProperty
    private PersonDirPrincipalResolverProperties principal = new PersonDirPrincipalResolverProperties();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

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

    public long getTolerance() {
        return Beans.newDuration(tolerance).toMillis();
    }

    public void setTolerance(final String tolerance) {
        this.tolerance = tolerance;
    }

    public String getAttributesType() {
        return attributesType;
    }

    public void setAttributesType(final String attributesType) {
        this.attributesType = attributesType;
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(final boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    public String getEncryptionPrivateKey() {
        return encryptionPrivateKey;
    }

    public void setEncryptionPrivateKey(final String encryptionPrivateKey) {
        this.encryptionPrivateKey = encryptionPrivateKey;
    }

    public String getEncryptionCertificate() {
        return encryptionCertificate;
    }

    public void setEncryptionCertificate(final String encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
    }

    public String getEncryptionPrivateKeyPassword() {
        return encryptionPrivateKeyPassword;
    }

    public void setEncryptionPrivateKeyPassword(final String encryptionPrivateKeyPassword) {
        this.encryptionPrivateKeyPassword = encryptionPrivateKeyPassword;
    }
}
