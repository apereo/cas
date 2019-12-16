package org.apereo.cas.config;

import org.apereo.cas.authentication.rest.SurrogateAuthenticatorRestHttpRequestCredentialFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateAuthenticationRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "surrogateAuthenticationRestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = RestHttpRequestCredentialFactoryConfigurer.class)
public class SurrogateAuthenticationRestConfiguration {
    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService;

    @Bean
    public RestHttpRequestCredentialFactoryConfigurer surrogateAuthenticatorRestHttpRequestCredentialFactoryConfigurer() {
        return factory -> factory.registerCredentialFactory(
            new SurrogateAuthenticatorRestHttpRequestCredentialFactory(surrogateAuthenticationService.getObject()));
    }
}
