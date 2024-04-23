package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * This is {@link CasRegisteredServicesTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestConfiguration(value = "CasRegisteredServicesTestConfiguration", proxyBeanMethods = false)
public class CasRegisteredServicesTestConfiguration {

    @Bean
    public RegisteredServicePrincipalAttributesRepository cachingPrincipalAttributeRepository() {
        return new CachingPrincipalAttributesRepository("SECONDS", 20);
    }

    @ConditionalOnMissingBean(name = "inMemoryRegisteredServices")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public List inMemoryRegisteredServices() throws Exception {
        return RegisteredServiceTestUtils.getRegisteredServicesForTests();
    }
}
