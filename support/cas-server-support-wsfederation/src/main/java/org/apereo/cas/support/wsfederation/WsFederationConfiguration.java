package org.apereo.cas.support.wsfederation;

import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class gathers configuration information for the WS Federation Identity Provider.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@Getter
@Setter
public class WsFederationConfiguration implements Serializable {

    private static final long serialVersionUID = 2310859477512242659L;

    private static final String QUERYSTRING = "?wa=wsignin1.0&wtrealm=%s&wctx=%s";

    private transient Resource encryptionPrivateKey;
    private transient Resource encryptionCertificate;

    private String encryptionPrivateKeyPassword;
    private String identityAttribute;
    private String identityProviderIdentifier;
    private String identityProviderUrl;

    private transient List<Resource> signingCertificateResources = new ArrayList<>(0);

    private String relyingPartyIdentifier;

    private long tolerance;

    private boolean autoRedirect;

    private WsFedPrincipalResolutionAttributesType attributesType;

    private WsFederationAttributeMutator attributeMutator;

    private transient List<Credential> signingWallet;

    private String name;
    private String id = UUID.randomUUID().toString();

    private transient CasCookieBuilder cookieGenerator;

    /**
     * getSigningCredential loads up an X509Credential from a file.
     *
     * @param resource the signing certificate file
     * @return an X509 credential
     */
    private static Credential getSigningCredential(final Resource resource) {
        try (val inputStream = resource.getInputStream()) {
            val certificateFactory = CertificateFactory.getInstance("X.509");
            val certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            val publicCredential = new BasicX509Credential(certificate);
            LOGGER.debug("Signing credential key retrieved from [{}].", resource);
            return publicCredential;
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String getName() {
        return StringUtils.isBlank(this.name) ? getClass().getSimpleName() : this.name;
    }

    public void initialize() {
        this.signingCertificateResources.forEach(Unchecked.consumer(r -> {
            try {
                val watcher = new FileWatcherService(r.getFile(), file -> createSigningWallet(this.signingCertificateResources));
                watcher.start(getClass().getSimpleName());
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            }
        }));
        createSigningWallet(this.signingCertificateResources);
    }

    /**
     * gets the signing certificates.
     *
     * @return X509credentials of the signing certs
     */
    public List<Credential> getSigningWallet() {
        if (this.signingWallet == null) {
            createSigningWallet(this.signingCertificateResources);
        }
        return this.signingWallet;
    }

    /**
     * sets the signing certs.
     *
     * @param signingCertificateResources a list of certificate files to read in.
     */
    public void setSigningCertificateResources(final Resource... signingCertificateResources) {
        this.signingCertificateResources = CollectionUtils.wrapList(signingCertificateResources);
        createSigningWallet(this.signingCertificateResources);
    }

    private void createSigningWallet(final List<Resource> signingCertificateFiles) {
        this.signingWallet = signingCertificateFiles.stream().map(WsFederationConfiguration::getSigningCredential).collect(Collectors.toList());
    }

    /**
     * Gets authorization url.
     *
     * @param relyingPartyIdentifier the relying party identifier
     * @param wctx                   the wctx
     * @return the authorization url
     */
    public String getAuthorizationUrl(final String relyingPartyIdentifier, final String wctx) {
        return String.format(getIdentityProviderUrl() + QUERYSTRING, relyingPartyIdentifier, wctx);
    }

    /**
     * Describes how the WS-FED principal resolution machinery
     * should process attributes from WS-FED.
     */
    public enum WsFedPrincipalResolutionAttributesType {

        /**
         * CAS ws fed principal resolution attributes type.
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

}
