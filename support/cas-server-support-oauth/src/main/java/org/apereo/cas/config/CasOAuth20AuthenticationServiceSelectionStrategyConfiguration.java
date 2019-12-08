package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuth20AuthenticationServiceSelectionStrategy;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOAuth20AuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casOAuth20AuthenticationServiceSelectionStrategyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20AuthenticationServiceSelectionStrategyConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Bean
    @ConditionalOnMissingBean(name = "oauth20AuthenticationRequestServiceSelectionStrategy")
    @RefreshScope
    public AuthenticationServiceSelectionStrategy oauth20AuthenticationRequestServiceSelectionStrategy() {
        return new OAuth20AuthenticationServiceSelectionStrategy(servicesManager.getObject(),
            webApplicationServiceFactory, OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()));
    }

    @Bean
    public AuthenticationServiceSelectionStrategyConfigurer oauth20AuthenticationServiceSelectionStrategyConfigurer() {
        return plan -> plan.registerStrategy(oauth20AuthenticationRequestServiceSelectionStrategy());
    }
}
