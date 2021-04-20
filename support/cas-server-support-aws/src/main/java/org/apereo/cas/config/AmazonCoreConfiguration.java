package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.aws.AmazonSecurityTokenServiceEndpoint;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link AmazonCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "AmazonCoreConfiguration", proxyBeanMethods = false)
public class AmazonCoreConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("restHttpRequestCredentialFactory")
    private ObjectProvider<RestHttpRequestCredentialFactory> restHttpRequestCredentialFactory;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedAuthenticationContextValidator> requestedContextValidator;

    @Bean
    @ConditionalOnAvailableEndpoint
    public AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint() {
        return new AmazonSecurityTokenServiceEndpoint(casProperties, authenticationSystemSupport.getObject(),
            restHttpRequestCredentialFactory.getObject(), requestedContextValidator.getObject());
    }
}
