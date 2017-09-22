package org.apereo.cas.support.wsfederation;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class gathers configuration information for the WS Federation Identity Provider.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class WsFederationConfiguration implements Serializable {
    private static final long serialVersionUID = 2310859477512242659L;

    private static final Logger LOGGER = LoggerFactory.getLogger(WsFederationConfiguration.class);

    /**
     * Describes how the WS-FED principal resolution machinery
     * should process attributes from WS-FED.
     */
    public enum WsFedPrincipalResolutionAttributesType {
        /**
         * Cas ws fed principal resolution attributes type.
         */
        CAS,
        /**
         * Wsfed ws fed principal resolution attributes type.
         */
        WSFED,
        /**
         * Both ws fed principal resolution attributes type.
         */
        BOTH
    }

    private Resource encryptionPrivateKey;
    
    private Resource encryptionCertificate;
    
    private String encryptionPrivateKeyPassword;
    
    private String identityAttribute;
    
    private String identityProviderIdentifier;

    private String identityProviderUrl;
    
    private List<Resource> signingCertificateResources = new ArrayList<>();
    
    private String relyingPartyIdentifier;
    
    private long tolerance;
    
    private WsFedPrincipalResolutionAttributesType attributesType;
    
    private WsFederationAttributeMutator attributeMutator;

    private List<Credential> signingWallet;

    @PostConstruct
    private void initCertificates() {
        createSigningWallet(this.signingCertificateResources);
    }

    /**
     * gets the identity of the IdP.
     *
     * @return the identity
     */
    public String getIdentityAttribute() {
        return this.identityAttribute;
    }

    /**
     * sets the identity of the IdP.
     *
     * @param identityAttribute the identity
     */
    public void setIdentityAttribute(final String identityAttribute) {
        this.identityAttribute = identityAttribute;
    }

    /**
     * gets the identity provider identifier.
     *
     * @return the identifier
     */
    public String getIdentityProviderIdentifier() {
        return this.identityProviderIdentifier;
    }

    /**
     * sets the identity provider identifier.
     *
     * @param identityProviderIdentifier the identifier.
     */
    public void setIdentityProviderIdentifier(final String identityProviderIdentifier) {
        this.identityProviderIdentifier = identityProviderIdentifier;
    }

    /**
     * gets the identity provider url.
     *
     * @return the url
     */
    public String getIdentityProviderUrl() {
        return this.identityProviderUrl;
    }

    /**
     * sets the identity provider url.
     *
     * @param identityProviderUrl the url
     */
    public void setIdentityProviderUrl(final String identityProviderUrl) {
        this.identityProviderUrl = identityProviderUrl;
    }

    /**
     * gets the relying part identifier.
     *
     * @return the identifier
     */
    public String getRelyingPartyIdentifier() {
        return this.relyingPartyIdentifier;
    }

    /**
     * sets the relying party identifier.
     *
     * @param relyingPartyIdentifier the identifier
     */
    public void setRelyingPartyIdentifier(final String relyingPartyIdentifier) {
        this.relyingPartyIdentifier = relyingPartyIdentifier;
    }

    /**
     * gets the signing certificates.
     *
     * @return X509credentials of the signing certs
     */
    public List<Credential> getSigningCertificates() {
        return this.signingWallet;
    }

    /**
     * gets the list of signing certificate files.
     *
     * @return the list of files
     */
    public List<Resource> getSigningCertificateResources() {
        return this.signingCertificateResources;
    }

    /**
     * sets the signing certs.
     *
     * @param signingCertificateResources a list of certificate files to read in.
     */
    public void setSigningCertificateResources(final Resource... signingCertificateResources) {
        this.signingCertificateResources = Arrays.asList(signingCertificateResources);
        createSigningWallet(this.signingCertificateResources);
    }

    private void createSigningWallet(final List<Resource> signingCertificateFiles) {
        this.signingWallet = signingCertificateFiles.stream().map(WsFederationConfiguration::getSigningCredential).collect(Collectors.toList());
    }

    /**
     * gets the tolerance.
     *
     * @return the tolerance in milliseconds
     */
    public long getTolerance() {
        return this.tolerance;
    }

    /**
     * sets the tolerance of the validity of the timestamp token.
     *
     * @param tolerance the tolerance in milliseconds
     */
    public void setTolerance(final long tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * gets the attributeMutator.
     *
     * @return an attributeMutator
     */
    public WsFederationAttributeMutator getAttributeMutator() {
        return this.attributeMutator;
    }

    /**
     * sets the attributeMutator.
     *
     * @param attributeMutator an attributeMutator
     */
    public void setAttributeMutator(final WsFederationAttributeMutator attributeMutator) {
        this.attributeMutator = attributeMutator;
    }

    public WsFedPrincipalResolutionAttributesType getAttributesType() {
        return this.attributesType;
    }

    public void setAttributesType(final WsFedPrincipalResolutionAttributesType attributesType) {
        this.attributesType = attributesType;
    }
    
    public void setSigningCertificateResources(final List<Resource> signingCertificateResources) {
        this.signingCertificateResources = signingCertificateResources;
    }

    public Resource getEncryptionPrivateKey() {
        return encryptionPrivateKey;
    }

    public void setEncryptionPrivateKey(final Resource encryptionPrivateKey) {
        this.encryptionPrivateKey = encryptionPrivateKey;
    }

    public Resource getEncryptionCertificate() {
        return encryptionCertificate;
    }

    public void setEncryptionCertificate(final Resource encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
    }

    public String getEncryptionPrivateKeyPassword() {
        return encryptionPrivateKeyPassword;
    }

    public void setEncryptionPrivateKeyPassword(final String encryptionPrivateKeyPassword) {
        this.encryptionPrivateKeyPassword = encryptionPrivateKeyPassword;
    }

    /**
     * getSigningCredential loads up an X509Credential from a file.
     *
     * @param resource the signing certificate file
     * @return an X509 credential
     */
    private static Credential getSigningCredential(final Resource resource) {
        try(InputStream inputStream = resource.getInputStream()) {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            final Credential publicCredential = new BasicX509Credential(certificate);
            LOGGER.debug("getSigningCredential: key retrieved.");
            return publicCredential;
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
}
