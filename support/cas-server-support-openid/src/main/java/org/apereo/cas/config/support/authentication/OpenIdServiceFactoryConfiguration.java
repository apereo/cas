package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link OpenIdServiceFactoryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("openIdServiceFactoryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OpenIdServiceFactoryConfiguration implements ServiceFactoryConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Override
    public Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories() {
        return CollectionUtils.wrap(openIdServiceFactory());
    }

    @Bean
    @RefreshScope
    public OpenIdServiceFactory openIdServiceFactory() {
        return new OpenIdServiceFactory(casProperties.getServer().getPrefix().concat("/openid"));
    }

}
