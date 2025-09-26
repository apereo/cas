package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.StartsWithRegisteredServiceMatchingStrategy;
import org.apereo.cas.support.oauth.services.OAuth20ServiceRegistry;
import org.apereo.cas.support.oauth.services.OAuth20ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link CasOAuth20ServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@Configuration(value = "CasOAuth20ServicesConfiguration", proxyBeanMethods = false)
class CasOAuth20ServicesConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "oauthServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer oauthServiceRegistryExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return plan -> {
            val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            val service = new CasRegisteredService();
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("OAuth Authentication Callback Request URL");
            service.setServiceId(String.format("^%s.*", callbackUrl));
            service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
            val matchingStrategy = new StartsWithRegisteredServiceMatchingStrategy().setExpectedUrl(callbackUrl);
            service.setMatchingStrategy(matchingStrategy);
            service.markAsInternal();
            plan.registerServiceRegistry(new OAuth20ServiceRegistry(applicationContext, service));
        };
    }

    @Configuration(value = "CasOAuth20ServicesCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    static class CasOAuth20ServicesCoreConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oauthServicesManagerRegisteredServiceLocator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServicesManagerRegisteredServiceLocator oauthServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
            return new OAuth20ServicesManagerRegisteredServiceLocator(casProperties);
        }
    }
}
