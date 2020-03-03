package org.apereo.cas.config.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryEntity;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.List;

/**
 * This is {@link JdbcPasswordHistoryManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("jdbcPasswordHistoryManagementConfiguration")
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.pm.history", name = "enabled", havingValue = "true")
public class JdbcPasswordHistoryManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    private ObjectProvider<DataSource> jdbcPasswordManagementDataSource;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaPasswordHistoryVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public List<String> jpaPasswordHistoryPackagesToScan() {
        return CollectionUtils.wrapList(JdbcPasswordHistoryEntity.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean passwordHistoryEntityManagerFactory() {
        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaPasswordHistoryVendorAdapter(),
            "jpaPasswordHistoryContext",
            jpaPasswordHistoryPackagesToScan(),
            jdbcPasswordManagementDataSource.getObject());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getPm().getJdbc());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerPasswordHistory(
        @Qualifier("passwordHistoryEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @RefreshScope
    @Bean
    public PasswordHistoryService passwordHistoryService() {
        return new JdbcPasswordHistoryService();
    }
}
