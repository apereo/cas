package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OpenIdServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 5.1.0
 */
@Configuration("openIdServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
public class OpenIdServiceFactoryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ServiceFactoryConfigurer openIdServiceFactoryConfigurer() {
        return () -> CollectionUtils.wrap(openIdServiceFactory());
    }

    @Bean
    @RefreshScope
    public OpenIdServiceFactory openIdServiceFactory() {
        return new OpenIdServiceFactory(casProperties.getServer().getPrefix().concat("/openid"));
    }

}
