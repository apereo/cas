package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonSecurityTokenServiceEndpoint;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.authentication.RestAuthenticationService;

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

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint(final CasConfigurationProperties casProperties,
                                                                              @Qualifier("restAuthenticationService")
                                                                              final RestAuthenticationService restAuthenticationService) {
        return new AmazonSecurityTokenServiceEndpoint(casProperties, restAuthenticationService);
    }
}
