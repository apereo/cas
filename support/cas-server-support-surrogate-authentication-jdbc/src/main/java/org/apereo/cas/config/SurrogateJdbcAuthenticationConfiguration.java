package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateJdbcAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class SurrogateJdbcAuthenticationConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public SurrogateAuthenticationService surrogateAuthenticationService() {
        val su = casProperties.getAuthn().getSurrogate();
        return new SurrogateJdbcAuthenticationService(su.getJdbc().getSurrogateSearchQuery(),
            surrogateAuthenticationJdbcDataSource(),
            su.getJdbc().getSurrogateAccountQuery(),
            servicesManager.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateAuthenticationJdbcDataSource")
    @RefreshScope
    public DataSource surrogateAuthenticationJdbcDataSource() {
        val su = casProperties.getAuthn().getSurrogate();
        return JpaBeans.newDataSource(su.getJdbc());
    }
}
