package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link OpenIdServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated 6.2
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
@Configuration(value = "openIdServiceFactoryConfiguration", proxyBeanMethods = false)
public class OpenIdServiceFactoryConfiguration {

    @Bean
    public ServiceFactoryConfigurer openIdServiceFactoryConfigurer(
        @Qualifier("openIdServiceFactory")
        final OpenIdServiceFactory openIdServiceFactory) {
        return () -> CollectionUtils.wrap(openIdServiceFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public OpenIdServiceFactory openIdServiceFactory(final CasConfigurationProperties casProperties) {
        return new OpenIdServiceFactory(casProperties.getServer().getPrefix().concat("/openid"));
    }
}
