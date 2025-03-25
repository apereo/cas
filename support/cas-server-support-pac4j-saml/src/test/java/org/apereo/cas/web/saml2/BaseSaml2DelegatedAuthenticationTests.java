package org.apereo.cas.web.saml2;

import org.apereo.cas.config.CasDelegatedAuthenticationSaml2AutoConfiguration;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * This is {@link BaseSaml2DelegatedAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class BaseSaml2DelegatedAuthenticationTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration(CasDelegatedAuthenticationSaml2AutoConfiguration.class)
    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
    public static class SharedTestConfiguration {

        @Bean
        @ConditionalOnProperty(name = "cas.custom.properties.delegation-test.enabled", havingValue = "true", matchIfMissing = true)
        public ConfigurableDelegatedClientBuilder saml2TestClientsBuilder() {
            return new Saml2TestClientsBuilder();
        }
    }


}
