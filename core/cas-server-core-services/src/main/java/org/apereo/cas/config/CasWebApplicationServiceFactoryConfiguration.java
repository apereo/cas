package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.UrlValidator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link CasWebApplicationServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication)
@Configuration(value = "CasWebApplicationServiceFactoryConfiguration", proxyBeanMethods = false)
class CasWebApplicationServiceFactoryConfiguration {

    @Configuration(value = "CasWebApplicationServiceFactoryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasWebApplicationServiceFactoryBaseConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = WebApplicationService.BEAN_NAME_FACTORY)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceFactory<WebApplicationService> webApplicationServiceFactory(
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor) {
            return new WebApplicationServiceFactory(tenantExtractor, urlValidator);
        }
    }

    @Configuration(value = "CasWebApplicationServiceFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasWebApplicationServiceFactoryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebApplicationServiceFactoryConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ServiceFactoryConfigurer casWebApplicationServiceFactoryConfigurer(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY) final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return () -> CollectionUtils.wrap(webApplicationServiceFactory);
        }

    }
}
