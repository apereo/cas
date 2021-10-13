package org.apereo.cas.config.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasWebApplicationServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasWebApplicationServiceFactoryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CasWebApplicationServiceFactoryConfiguration {

    @Configuration(value = "CasWebApplicationServiceFactoryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebApplicationServiceFactoryBaseConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "webApplicationServiceFactory")
        public ServiceFactory<WebApplicationService> webApplicationServiceFactory() {
            return new WebApplicationServiceFactory();
        }
    }

    @Configuration(value = "CasWebApplicationServiceFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebApplicationServiceFactoryPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebApplicationServiceFactoryConfigurer")
        @Autowired
        public ServiceFactoryConfigurer casWebApplicationServiceFactoryConfigurer(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return () -> CollectionUtils.wrap(webApplicationServiceFactory);
        }

    }
}
