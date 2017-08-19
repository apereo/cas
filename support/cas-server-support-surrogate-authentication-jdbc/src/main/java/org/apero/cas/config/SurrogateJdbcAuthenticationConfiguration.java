package org.apero.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateJdbcAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SurrogateJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("surrogateJdbcAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SurrogateJdbcAuthenticationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateJdbcAuthenticationConfiguration.class);

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        final SurrogateAuthenticationProperties su = casProperties.getAuthn().getSurrogate();
        return new SurrogateJdbcAuthenticationService(su.getJdbc().getSurrogateSearchQuery(),
                JpaBeans.newDataSource(su.getJdbc()),
                su.getJdbc().getSurrogateAccountQuery(), 
                servicesManager);
    }
}
