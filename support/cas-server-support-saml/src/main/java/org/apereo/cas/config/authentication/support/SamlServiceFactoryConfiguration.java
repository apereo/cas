package org.apereo.cas.config.authentication.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "samlServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlServiceFactoryConfiguration {

    @Bean
    public ServiceFactoryConfigurer samlServiceFactoryConfigurer() {
        return () -> CollectionUtils.wrap(samlServiceFactory());
    }

    @Bean
    public ServiceFactory<SamlService> samlServiceFactory() {
        return new SamlServiceFactory();
    }
}
