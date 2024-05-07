package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonSecurityTokenServiceEndpoint;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAmazonCoreAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core, module = "aws")
@AutoConfiguration
public class CasAmazonCoreAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint
    public AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint(
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<CasConfigurationProperties> casProperties,
        @Qualifier(RestAuthenticationService.DEFAULT_BEAN_NAME)
        final ObjectProvider<RestAuthenticationService> restAuthenticationService) {
        return new AmazonSecurityTokenServiceEndpoint(casProperties, applicationContext, restAuthenticationService);
    }
}
