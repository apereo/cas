package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "wsFederationConfiguration", proxyBeanMethods = false)
public class WsFederationAuthenticationConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationHelper")
    public WsFederationHelper wsFederationHelper(
        @Qualifier("configBean")
        final OpenSamlConfigBean configBean,
        @Qualifier("servicesManager")
        final ServicesManager servicesManager) {
        return new WsFederationHelper(configBean, servicesManager);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationCookieManager")
    @Autowired
    public WsFederationCookieManager wsFederationCookieManager(
        @Qualifier("wsFederationConfigurations")
        final Collection<WsFederationConfiguration> wsFederationConfigurations,
        final CasConfigurationProperties casProperties) {
        return new WsFederationCookieManager(wsFederationConfigurations,
            casProperties.getTheme().getParamName(), casProperties.getLocale().getParamName());
    }

    @Bean
    @Autowired
    public WsFederationNavigationController wsFederationNavigationController(
        @Qualifier("Collection<WsFederationConfiguration> wsFederationConfigurations")
        final Collection<WsFederationConfiguration> wsFederationConfigurations,
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        final CasConfigurationProperties casProperties,
        @Qualifier("wsFederationCookieManager")
        final WsFederationCookieManager wsFederationCookieManager,
        @Qualifier("wsFederationHelper")
        final WsFederationHelper wsFederationHelper,
        @Qualifier("authenticationServiceSelectionPlan")
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier("argumentExtractor")
        final ArgumentExtractor argumentExtractor) {
        return new WsFederationNavigationController(wsFederationCookieManager,
            wsFederationHelper, wsFederationConfigurations, authenticationRequestServiceSelectionStrategies,
            webApplicationServiceFactory, casProperties.getServer().getLoginUrl(), argumentExtractor);
    }
}
