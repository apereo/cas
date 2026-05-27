package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.SurrogateEndpoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SurrogateAuthenticationWebConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
@Configuration(value = "SurrogateAuthenticationWebConfiguration", proxyBeanMethods = false)
class SurrogateAuthenticationWebConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnAvailableEndpoint
    public SurrogateEndpoint surrogateEndpoint(
        @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
        final ObjectProvider<SurrogateAuthenticationService> surrogateAuthenticationService,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new SurrogateEndpoint(casProperties, applicationContext, surrogateAuthenticationService);
    }
}
