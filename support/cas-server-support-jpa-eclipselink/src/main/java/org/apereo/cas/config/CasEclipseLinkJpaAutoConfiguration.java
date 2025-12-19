package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.eclipselink.CasEclipseLinkJpaBeanFactory;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasEclipseLinkJpaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.JDBC)
@AutoConfiguration
public class CasEclipseLinkJpaAutoConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public JpaBeanFactory jpaBeanFactory() {
        return new CasEclipseLinkJpaBeanFactory();
    }
}
