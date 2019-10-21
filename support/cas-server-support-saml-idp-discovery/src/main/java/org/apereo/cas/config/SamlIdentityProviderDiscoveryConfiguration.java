package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.IdentityProviderDiscoveryFeedController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdentityProviderDiscoveryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "samlIdentityProviderDiscoveryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdentityProviderDiscoveryConfiguration {

    @ConditionalOnMissingBean(name = "identityProviderDiscoveryFeedController")
    @Bean
    public IdentityProviderDiscoveryFeedController identityProviderDiscoveryFeedController() {
        return new IdentityProviderDiscoveryFeedController();
    }
}
