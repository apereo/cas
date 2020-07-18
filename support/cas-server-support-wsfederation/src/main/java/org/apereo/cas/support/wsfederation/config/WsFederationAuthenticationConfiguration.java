package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("wsFederationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFederationAuthenticationConfiguration {

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> configBean;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("wsFederationConfigurations")
    private Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    private CasConfigurationProperties casProperties;


    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;
    
    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationHelper")
    public WsFederationHelper wsFederationHelper() {
        return new WsFederationHelper(configBean.getObject(), servicesManager.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "wsFederationCookieManager")
    public WsFederationCookieManager wsFederationCookieManager() {
        return new WsFederationCookieManager(wsFederationConfigurations,
            casProperties.getTheme().getParamName(), casProperties.getLocale().getParamName());
    }

    @Bean
    public WsFederationNavigationController wsFederationNavigationController() {
        return new WsFederationNavigationController(
            wsFederationCookieManager(),
            wsFederationHelper(),
            wsFederationConfigurations,
            authenticationRequestServiceSelectionStrategies.getObject(),
            webApplicationServiceFactory.getObject(),
            casProperties.getServer().getLoginUrl(),
            argumentExtractor.getObject());
    }
}
