package org.apereo.cas.config.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link CasWebApplicationServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("webApplicationServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebApplicationServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Bean
    public ServiceFactory webApplicationServiceFactory() {
        return new WebApplicationServiceFactory();
    }

    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(webApplicationServiceFactory());
    }
}
