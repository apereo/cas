package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuth20AuthenticationServiceSelectionStrategy;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasOAuth20AuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casOAuth20AuthenticationServiceSelectionStrategyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20AuthenticationServiceSelectionStrategyConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "oauth20AuthenticationRequestServiceSelectionStrategy")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy(
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        return new OAuth20AuthenticationServiceSelectionStrategy(servicesManager,
            webApplicationServiceFactory, OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()));
    }

    @Bean
    @Autowired
    public AuthenticationServiceSelectionStrategyConfigurer oauth20AuthenticationServiceSelectionStrategyConfigurer(
        @Qualifier("oauth20AuthenticationRequestServiceSelectionStrategy")
        final AuthenticationServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy) {
        return plan -> plan.registerStrategy(oauth20AuthenticationRequestServiceSelectionStrategy);
    }
}
