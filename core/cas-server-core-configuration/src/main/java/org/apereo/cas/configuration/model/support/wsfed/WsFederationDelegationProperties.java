package org.apereo.cas.configuration.model.support.wsfed;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link WsFederationDelegationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-wsfederation-webflow")
public class WsFederationDelegationProperties implements Serializable {

    private static final long serialVersionUID = 5743971334977239938L;
    /**
     * The attribute extracted from the assertion and used to construct the CAS principal id.
     */
    @RequiredProperty
    private String identityAttribute = "upn";
    /**
     * The entity id or the identifier of the Wsfed instance.
     */
    @RequiredProperty
    private String identityProviderIdentifier = "https://adfs.example.org/adfs/services/trust";
    /**
     * Wsfed identity provider url.
     */
    @RequiredProperty
    private String identityProviderUrl = "https://adfs.example.org/adfs/ls/";
    /**
     * Locations of signing certificates used to verify assertions.
     */
    @RequiredProperty
    private String signingCertificateResources = "classpath:adfs-signing.crt";
    /**
     * The identifier for CAS (RP) registered with wsfed.
     */
    @RequiredProperty
    private String relyingPartyIdentifier = "urn:cas:localhost";
    /**
     * Tolerance value used to skew assertions to support clock drift.
     */
    private String tolerance = "PT10S";
    /**
     * Indicates how attributes should be recorded into the principal object. 
     * Useful if you wish to additionally resolve attributes on top of what wsfed provides.
     * Accepted values are {@code CAS,WSFED,BOTH}.
     */
    private String attributesType = "WSFED";

    /**
     * Whether CAS should enable its own attribute resolution machinery
     * after having received a response from wsfed.
     */
    private boolean attributeResolverEnabled = true;

    /**
     * Whether CAS should auto redirect to this wsfed instance.
     */
    private boolean autoRedirect = true;

    /**
     * The path to the private key used to handle and verify encrypted assertions.
     */
    private String encryptionPrivateKey = "classpath:private.key";
    /**
     * The path to the public key/certificate used to handle and verify encrypted assertions.
     */
    private String encryptionCertificate = "classpath:certificate.crt";
    /**
     * The private key password.
     */
    private String encryptionPrivateKeyPassword = "NONE";

    /**
     * Principal resolution settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PersonDirectoryPrincipalResolverProperties getPrincipal() {
        return principal;
    }

    public void setPrincipal(final PersonDirectoryPrincipalResolverProperties principal) {
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
