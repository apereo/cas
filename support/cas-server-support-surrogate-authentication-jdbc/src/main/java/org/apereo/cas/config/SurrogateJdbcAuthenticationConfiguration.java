package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateJdbcAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link SurrogateJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("surrogateJdbcAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SurrogateJdbcAuthenticationConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        final var su = casProperties.getAuthn().getSurrogate();
        return new SurrogateJdbcAuthenticationService(su.getJdbc().getSurrogateSearchQuery(),
            surrogateAuthenticationJdbcDataSource(),
            su.getJdbc().getSurrogateAccountQuery(),
            servicesManager);
    }

    @Bean
    public DataSource surrogateAuthenticationJdbcDataSource() {
        final var su = casProperties.getAuthn().getSurrogate();
        return JpaBeans.newDataSource(su.getJdbc());
    }
}
