package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.SamlIdpApplicationServiceFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * Configuration class for Saml20WebapplicationServiceFactory.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Configuration("samlIdpApplicationServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdpWebApplicationServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Bean
    public ServiceFactory samlIdpApplicationServiceFactory() {
        return new SamlIdpApplicationServiceFactory();
    }

    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(samlIdpApplicationServiceFactory());
    }
}
