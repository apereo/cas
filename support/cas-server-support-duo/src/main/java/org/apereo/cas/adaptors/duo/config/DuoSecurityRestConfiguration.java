package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.config.cond.ConditionalOnDuoSecurityConfigured;
import org.apereo.cas.adaptors.duo.rest.DuoSecurityRestHttpRequestCredentialFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DuoSecurityRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "duoSecurityRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnDuoSecurityConfigured
@ConditionalOnClass(value = RestHttpRequestCredentialFactoryConfigurer.class)
public class DuoSecurityRestConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityRestHttpRequestCredentialFactoryConfigurer")
    @Autowired
    public RestHttpRequestCredentialFactoryConfigurer duoSecurityRestHttpRequestCredentialFactoryConfigurer(
        @Qualifier("duoSecurityRestHttpRequestCredentialFactory") final RestHttpRequestCredentialFactory duoSecurityRestHttpRequestCredentialFactory) {
        return factory -> factory.registerCredentialFactory(duoSecurityRestHttpRequestCredentialFactory);
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityRestHttpRequestCredentialFactory")
    public RestHttpRequestCredentialFactory duoSecurityRestHttpRequestCredentialFactory() {
        return new DuoSecurityRestHttpRequestCredentialFactory();
    }
}
