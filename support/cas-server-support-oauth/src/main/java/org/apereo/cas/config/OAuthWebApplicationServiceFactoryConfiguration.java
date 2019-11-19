package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.authentication.principal.OAuthApplicationServiceFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * Configuration class for OAuthWebapplicationServiceFactory.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@Configuration("oauthApplicationServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OAuthWebApplicationServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Bean
    public ServiceFactory oauthApplicationServiceFactory() {
        return new OAuthApplicationServiceFactory();
    }

    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(oauthApplicationServiceFactory());
    }
}
