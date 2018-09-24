package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.pm.JdbcPasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link JdbcPasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("jdbcPasswordManagementConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JdbcPasswordManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private ObjectProvider<CipherExecutor> passwordManagementCipherExecutor;

    @Bean
    public DataSource jdbcPasswordManagementDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getPm().getJdbc());
    }

    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        return new JdbcPasswordManagementService(passwordManagementCipherExecutor.getIfAvailable(),
            casProperties.getServer().getPrefix(),
            casProperties.getAuthn().getPm(),
            jdbcPasswordManagementDataSource());
    }
}
