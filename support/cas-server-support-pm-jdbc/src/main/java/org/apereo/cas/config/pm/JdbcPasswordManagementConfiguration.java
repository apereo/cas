package org.apereo.cas.config.pm;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.jdbc.JdbcPasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@Configuration("jdbcPasswordManagementConfiguration")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JdbcPasswordManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Autowired
    @Qualifier("passwordManagementCipherExecutor")
    private ObjectProvider<CipherExecutor> passwordManagementCipherExecutor;

    @Autowired
    @Qualifier("passwordHistoryService")
    private ObjectProvider<PasswordHistoryService> passwordHistoryService;

    @Bean
    @ConditionalOnMissingBean(name = "jdbcPasswordManagementDataSource")
    @RefreshScope
    public DataSource jdbcPasswordManagementDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getPm().getJdbc());
    }

    @Bean
    public PlatformTransactionManager jdbcPasswordManagementTransactionManager() {
        return new DataSourceTransactionManager(jdbcPasswordManagementDataSource());
    }

    @ConditionalOnMissingBean(name = "jdbcPasswordManagementTransactionTemplate")
    @Bean
    public TransactionTemplate jdbcPasswordManagementTransactionTemplate() {
        val t = new TransactionTemplate(jdbcPasswordManagementTransactionManager());
        t.setIsolationLevelName(casProperties.getAuthn().getPm().getJdbc().getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAuthn().getPm().getJdbc().getPropagationBehaviorName());
        return t;
    }
    
    @RefreshScope
    @Bean
    public PasswordManagementService passwordChangeService() {
        val encoder = PasswordEncoderUtils.newPasswordEncoder(casProperties.getAuthn().getPm().getJdbc().getPasswordEncoder(), applicationContext);
        return new JdbcPasswordManagementService(passwordManagementCipherExecutor.getObject(),
            casProperties.getServer().getPrefix(),
            casProperties.getAuthn().getPm(),
            jdbcPasswordManagementDataSource(),
            jdbcPasswordManagementTransactionTemplate(),
            passwordHistoryService.getObject(),
            encoder);
    }
}
