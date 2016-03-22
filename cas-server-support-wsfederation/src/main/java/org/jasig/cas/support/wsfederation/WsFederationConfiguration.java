package org.jasig.cas.support.wsfederation;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
@RefreshScope
@Component("wsFedConfig")
public class WsFederationConfiguration implements Serializable {
    private static final long serialVersionUID = 2310859477512242659L;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

    
    @Value("${cas.wsfed.idp.idattribute:upn}")
    private String identityAttribute;

    
    @Value("${cas.wsfed.idp.id:https://adfs.example.org/adfs/services/trust}")
    private String identityProviderIdentifier;

    
    @Value("${cas.wsfed.idp.url:https://adfs.example.org/adfs/ls/}")
    private String identityProviderUrl;

    
    @Value("#{'${cas.wsfed.idp.signingcerts:classpath:adfs-signing.crt}'.split(',')}")
    private List<Resource> signingCertificateResources;

    
    @Value("${cas.wsfed.rp.id:urn:cas:localhost}")
    private String relyingPartyIdentifier;

    @Value("${cas.wsfed.idp.tolerance:10000}")
    private int tolerance;

    @Value("${cas.wsfed.idp.attribute.resolver.type:WSFED}")
    private WsFedPrincipalResolutionAttributesType attributesType;

    @Autowired(required=false)
    @Qualifier("wsfedAttributeMutator")
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
        return identityProviderIdentifier;
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
        this.signingWallet = signingCertificateFiles.stream().map(this::getSigningCredential).collect(Collectors.toList());
    }

    /**
     * gets the tolerance.
     *
     * @return the tolerance in milliseconds
     */
    public int getTolerance() {
        return tolerance;
    }

    /**
     * sets the tolerance of the validity of the timestamp token.
     *
     * @param tolerance the tolerance in milliseconds
     */
    public void setTolerance(final int tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * gets the attributeMutator.
     *
     * @return an attributeMutator
     */
    public WsFederationAttributeMutator getAttributeMutator() {
        return attributeMutator;
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
        return attributesType;
    }

    public void setAttributesType(final WsFedPrincipalResolutionAttributesType attributesType) {
        this.attributesType = attributesType;
    }

    /**
     * getSigningCredential loads up an X509Credential from a file.
     *
     * @param resource the signing certificate file
     * @return an X509 credential
     */
    private Credential getSigningCredential(final Resource resource) {
        try (final InputStream inputStream = resource.getInputStream()) {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            final Credential publicCredential = new BasicX509Credential(certificate);
            logger.debug("getSigningCredential: key retrieved.");
            return publicCredential;
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }
}
