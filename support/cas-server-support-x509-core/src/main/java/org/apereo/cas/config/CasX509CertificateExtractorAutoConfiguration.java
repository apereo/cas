package org.apereo.cas.config;

import org.apereo.cas.adaptors.x509.authentication.RequestHeaderX509CertificateExtractor;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This class configures the {@link X509CertificateExtractor} for the x509 webflow and x509 rest
 * authentication modules.
 *
 * @author Curtis W Ruck
 * @since 5.3.3
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.X509)
@AutoConfiguration
public class CasX509CertificateExtractorAutoConfiguration {

    @ConditionalOnMissingBean(name = "x509CertificateExtractor")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public X509CertificateExtractor x509CertificateExtractor(final CasConfigurationProperties casProperties) {
        val sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
        return new RequestHeaderX509CertificateExtractor(sslHeaderName);
    }
}
