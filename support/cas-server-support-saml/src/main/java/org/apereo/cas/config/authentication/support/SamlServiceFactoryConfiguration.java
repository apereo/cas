package org.apereo.cas.config.authentication.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Collections;

/**
 * This is {@link SamlServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("samlServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return Collections.singleton(samlServiceFactory());
    }


    @Bean
    public ServiceFactory<SamlService> samlServiceFactory() {
        return new SamlServiceFactory();
    }

}
