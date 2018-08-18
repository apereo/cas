package org.apereo.cas.web.extractcert;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 5.3.3
 */
@Configuration("x509CertificateExtractorConfig")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
class X509CertificateExtractorConfig {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "x509ExtractSSLCertificate")
    @Bean
    public X509CertificateExtractor x509ExtractSSLCertificate() {
        val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
        return new RequestHeaderX509CertificateExtractor(sslHeaderName);
    }
}
