package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.services.WSFederationAuthenticationServiceRegistry;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederation)
@AutoConfiguration
public class WsFederationAuthenticationConfiguration {

    @Configuration(value = "WsFederationAuthenticationHelperConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WsFederationAuthenticationHelperConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "wsFederationHelper")
        public WsFederationHelper wsFederationHelper(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean configBean,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new WsFederationHelper(configBean, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "wsFederationAuthenticationServiceRegistryExecutionPlanConfigurer")
        public ServiceRegistryExecutionPlanConfigurer wsFederationAuthenticationServiceRegistryExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return plan -> {
                val service = new CasRegisteredService();
                service.setId(RandomUtils.nextLong());
                service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
                service.setName(service.getClass().getSimpleName());
                service.setDescription("WS-Federation Authentication Request");
                service.setServiceId("^".concat(casProperties.getServer().getPrefix()).concat(".+"));
                plan.registerServiceRegistry(new WSFederationAuthenticationServiceRegistry(applicationContext, service));
            };
        }
    }

    @Configuration(value = "WsFederationAuthenticationCookieConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WsFederationAuthenticationCookieConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "wsFederationCookieManager")
        public WsFederationCookieManager wsFederationCookieManager(
            @Qualifier("wsFederationConfigurations")
            final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
            final CasConfigurationProperties casProperties) {
            return new WsFederationCookieManager(wsFederationConfigurations.toList(), casProperties);
        }

    }

    @Configuration(value = "WsFederationAuthenticationControllerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WsFederationAuthenticationControllerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public WsFederationNavigationController wsFederationNavigationController(
            @Qualifier("wsFederationConfigurations")
            final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            final CasConfigurationProperties casProperties,
            @Qualifier("wsFederationCookieManager")
            final WsFederationCookieManager wsFederationCookieManager,
            @Qualifier("wsFederationHelper")
            final WsFederationHelper wsFederationHelper,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor) {
            return new WsFederationNavigationController(wsFederationCookieManager,
                wsFederationHelper, wsFederationConfigurations.toList(),
                webApplicationServiceFactory, casProperties.getServer().getLoginUrl(), argumentExtractor);
        }
    }
}
