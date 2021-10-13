package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateJdbcAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * This is {@link SurrogateJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "surrogateJdbcAuthenticationConfiguration", proxyBeanMethods = false)
public class SurrogateJdbcAuthenticationConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public SurrogateAuthenticationService surrogateAuthenticationService(final CasConfigurationProperties casProperties,
                                                                         @Qualifier("surrogateAuthenticationJdbcDataSource")
                                                                         final DataSource surrogateAuthenticationJdbcDataSource,
                                                                         @Qualifier(ServicesManager.BEAN_NAME)
                                                                         final ServicesManager servicesManager) {
        val su = casProperties.getAuthn().getSurrogate();
        return new SurrogateJdbcAuthenticationService(su.getJdbc().getSurrogateSearchQuery(), new JdbcTemplate(surrogateAuthenticationJdbcDataSource),
            su.getJdbc().getSurrogateAccountQuery(), servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateAuthenticationJdbcDataSource")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public DataSource surrogateAuthenticationJdbcDataSource(final CasConfigurationProperties casProperties) {
        val su = casProperties.getAuthn().getSurrogate();
        return JpaBeans.newDataSource(su.getJdbc());
    }
}
