package org.apereo.cas.config.authentication.support;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link SamlServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("samlServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Autowired
    @Qualifier("saml10ObjectBuilder")
    private ObjectProvider<Saml10ObjectBuilder> saml10ObjectBuilder;

    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(samlServiceFactory());
    }

    @Bean
    public ServiceFactory<SamlService> samlServiceFactory() {
        return new SamlServiceFactory(this.saml10ObjectBuilder.getIfAvailable());
    }
}
