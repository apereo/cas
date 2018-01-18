package org.apereo.cas.support.wsfederation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.core.io.Resource;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

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

    /**
     * Describes how the WS-FED principal resolution machinery
     * should process attributes from WS-FED.
     */
    public enum WsFedPrincipalResolutionAttributesType {

        /**
         * Cas ws fed principal resolution attributes type.
         */
        CAS, /**
         * Wsfed ws fed principal resolution attributes type.
         */
        WSFED, /**
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

    private boolean autoRedirect;

    private WsFedPrincipalResolutionAttributesType attributesType;

    private WsFederationAttributeMutator attributeMutator;

    private List<Credential> signingWallet;

    private String name;

    public String getName() {
        return StringUtils.isBlank(this.name) ? getClass().getSimpleName() : this.name;
    }

    public void initialize() {
        this.signingCertificateResources.stream().forEach(Unchecked.consumer(r -> {
            try {
                final FileWatcherService watcher = new FileWatcherService(r.getFile(), file -> createSigningWallet(this.signingCertificateResources));
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
     * getSigningCredential loads up an X509Credential from a file.
     *
     * @param resource the signing certificate file
     * @return an X509 credential
     */
    private static Credential getSigningCredential(final Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            final Credential publicCredential = new BasicX509Credential(certificate);
            LOGGER.debug("Signing credential key retrieved from [{}].", resource);
            return publicCredential;
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
}
