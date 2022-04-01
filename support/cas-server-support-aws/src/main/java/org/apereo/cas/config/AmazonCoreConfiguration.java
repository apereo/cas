package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonSecurityTokenServiceEndpoint;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link AmazonCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "AmazonCoreConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Core, module = "aws")
public class AmazonCoreConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint
    public AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint(
        final CasConfigurationProperties casProperties,
        @Qualifier(RestAuthenticationService.DEFAULT_BEAN_NAME)
        final RestAuthenticationService restAuthenticationService) {
        return new AmazonSecurityTokenServiceEndpoint(casProperties, restAuthenticationService);
    }
}
