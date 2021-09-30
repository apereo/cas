package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuth20ServiceRegistry;
import org.apereo.cas.support.oauth.services.OAuth20ServicesManagerRegisteredServiceLocator;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(value = "casOAuth20ServicesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20ServicesConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    @ConditionalOnMissingBean(name = "oauthServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer oauthServiceRegistryExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return plan -> {
            val oAuthCallbackUrl = casProperties.getServer().getPrefix()
                + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
            val service = new RegexRegisteredService();
            service.setId(RandomUtils.nextLong());
            service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("OAuth Authentication Callback Request URL");
            service.setServiceId(oAuthCallbackUrl);
            service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
            plan.registerServiceRegistry(new OAuth20ServiceRegistry(applicationContext, service));
        };
    }

    @Configuration(value = "CasOAuth20ServicesCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public static class CasOAuth20ServicesCoreConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oauthServicesManagerRegisteredServiceLocator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServicesManagerRegisteredServiceLocator oauthServicesManagerRegisteredServiceLocator() {
            return new OAuth20ServicesManagerRegisteredServiceLocator();
        }
    }
}
