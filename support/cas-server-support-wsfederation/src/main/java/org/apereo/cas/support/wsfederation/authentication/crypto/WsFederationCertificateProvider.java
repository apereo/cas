package org.apereo.cas.support.wsfederation.authentication.crypto;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * This is {@link WsFederationCertificateProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface WsFederationCertificateProvider {
    /**
     * Gets providers.
     *
     * @param config             the config
     * @param openSamlConfigBean the open saml config bean
     * @return the providers
     */
    static WsFederationCertificateProvider getProvider(final WsFederationConfiguration config,
                                                       final OpenSamlConfigBean openSamlConfigBean) {
        val chain = new ChainingWsFederationCertificateProvider();
        StringUtils.commaDelimitedListToSet(config.getSigningCertificates())
            .stream()
            .map(Unchecked.function(ResourceUtils::getRawResourceFrom))
            .forEach(resource -> {
                if (StringUtils.hasText(resource.getFilename()) && resource.getFilename().endsWith(".xml")) {
                    chain.addProvider(new WsFederationMetadataCertificateProvider(resource, config, openSamlConfigBean));
                } else {
                    chain.addProvider(new WsFederationStaticCertificateProvider(resource));
                }
            });
        return chain;
    }

    /**
     * Read credential.
     *
     * @param is the stream
     * @return the credential
     * @throws Exception the exception
     */
    static Credential readCredential(final InputStream is) throws Exception {
        val certificateFactory = CertUtils.getCertificateFactory();
        val certificate = (X509Certificate) certificateFactory.generateCertificate(is);
        return new BasicX509Credential(certificate);
    }

    /**
     * Gets signing credentials.
     *
     * @return the signing credentials
     * @throws Exception the exception
     */
    List<Credential> getSigningCredentials() throws Exception;
}
