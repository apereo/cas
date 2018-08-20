package org.apereo.cas.web.extractcert;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class configures the {@link X509CertificateExtractor} for the x509 webflow and x509 rest
 * authentication modules.
 *
 * @author Curtis W. Ruck
 * @since 5.3.3
 */
@Configuration("x509CertificateExtractorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509CertificateExtractorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "x509ExtractSSLCertificate")
    @Bean
    public X509CertificateExtractor x509ExtractSSLCertificate() {
        final String sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
        return new RequestHeaderX509CertificateExtractor(sslHeaderName);
    }
}
