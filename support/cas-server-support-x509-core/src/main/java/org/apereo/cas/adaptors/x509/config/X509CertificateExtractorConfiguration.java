package org.apereo.cas.adaptors.x509.config;

import org.apereo.cas.adaptors.x509.authentication.RequestHeaderX509CertificateExtractor;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class configures the {@link X509CertificateExtractor} for the x509 webflow and x509 rest
 * authentication modules.
 *
 * @author Curtis W Ruck
 * @since 5.3.3
 */
@Configuration(value = "x509CertificateExtractorConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509CertificateExtractorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "x509CertificateExtractor")
    @Bean
    @RefreshScope
    public X509CertificateExtractor x509CertificateExtractor() {
        val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
        return new RequestHeaderX509CertificateExtractor(sslHeaderName);
    }
}
