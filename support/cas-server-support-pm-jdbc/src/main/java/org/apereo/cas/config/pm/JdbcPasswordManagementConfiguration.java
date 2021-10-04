package org.apereo.cas.config.pm;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.jdbc.JdbcPasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * This is {@link JdbcPasswordManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "JdbcPasswordManagementConfiguration", proxyBeanMethods = false)
public class JdbcPasswordManagementConfiguration {

    @Configuration(value = "JdbcPasswordManagementServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordManagementServiceConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "jdbcPasswordChangeService")
        @Autowired
        public PasswordManagementService passwordChangeService(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jdbcPasswordManagementDataSource")
            final DataSource jdbcPasswordManagementDataSource,
            @Qualifier("jdbcPasswordManagementTransactionTemplate")
            final TransactionTemplate jdbcPasswordManagementTransactionTemplate,
            @Qualifier("passwordManagementCipherExecutor")
            final CipherExecutor passwordManagementCipherExecutor,
            @Qualifier("passwordHistoryService")
            final PasswordHistoryService passwordHistoryService) {
            val encoder = PasswordEncoderUtils.newPasswordEncoder(
                casProperties.getAuthn().getPm().getJdbc().getPasswordEncoder(), applicationContext);
            return new JdbcPasswordManagementService(passwordManagementCipherExecutor,
                casProperties.getServer().getPrefix(), casProperties.getAuthn().getPm(), jdbcPasswordManagementDataSource,
                jdbcPasswordManagementTransactionTemplate, passwordHistoryService, encoder);
        }

    }

    @Configuration(value = "JdbcPasswordManagementDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordManagementDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jdbcPasswordManagementDataSource")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource jdbcPasswordManagementDataSource(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getPm().getJdbc());
        }
    }

    @Configuration(value = "JdbcPasswordManagementTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordManagementTransactionConfiguration {
        @Bean
        public PlatformTransactionManager jdbcPasswordManagementTransactionManager(
            @Qualifier("jdbcPasswordManagementDataSource")
            final DataSource jdbcPasswordManagementDataSource) {
            return new DataSourceTransactionManager(jdbcPasswordManagementDataSource);
        }

        @ConditionalOnMissingBean(name = "jdbcPasswordManagementTransactionTemplate")
        @Bean
        @Autowired
        public TransactionTemplate jdbcPasswordManagementTransactionTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier("jdbcPasswordManagementTransactionManager")
            final PlatformTransactionManager jdbcPasswordManagementTransactionManager) {
            val t = new TransactionTemplate(jdbcPasswordManagementTransactionManager);
            t.setIsolationLevelName(casProperties.getAuthn().getPm().getJdbc().getIsolationLevelName());
            t.setPropagationBehaviorName(casProperties.getAuthn().getPm().getJdbc().getPropagationBehaviorName());
            return t;
        }

    }
}
