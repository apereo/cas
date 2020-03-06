package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.eclipselink.CasEclipseLinkJpaBeanFactory;
import org.apereo.cas.jpa.JpaBeanFactory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEclipseLinkJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "casEclipseLinkJpaConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasEclipseLinkJpaConfiguration {
    @RefreshScope
    @Bean
    public JpaBeanFactory jpaBeanFactory() {
        return new CasEclipseLinkJpaBeanFactory();
    }
}
