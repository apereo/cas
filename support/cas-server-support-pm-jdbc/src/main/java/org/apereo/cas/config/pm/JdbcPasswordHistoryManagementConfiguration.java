package org.apereo.cas.config.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.impl.history.PasswordHistoryEntity;
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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.pm.history", name = "enabled", havingValue = "true")
public class JdbcPasswordHistoryManagementConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    private ObjectProvider<DataSource> jdbcPasswordManagementDataSource;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaPasswordHistoryVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    public List<String> jpaPasswordHistoryPackagesToScan() {
        return CollectionUtils.wrapList(PasswordHistoryEntity.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean passwordHistoryEntityManagerFactory() {
        return JpaBeans.newHibernateEntityManagerFactoryBean(
            new JpaConfigDataHolder(
                jpaPasswordHistoryVendorAdapter(),
                "jpaPasswordHistoryContext",
                jpaPasswordHistoryPackagesToScan(),
                jdbcPasswordManagementDataSource.getObject()),
            casProperties.getAuthn().getPm().getJdbc());
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
