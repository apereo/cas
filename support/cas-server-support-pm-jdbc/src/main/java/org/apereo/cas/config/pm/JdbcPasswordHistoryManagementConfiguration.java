package org.apereo.cas.config.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryEntity;
import org.apereo.cas.pm.jdbc.JdbcPasswordHistoryService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link JdbcPasswordHistoryManagementConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.pm.history.core", name = "enabled", havingValue = "true")
@Configuration(value = "JdbcPasswordHistoryManagementConfiguration", proxyBeanMethods = false)
public class JdbcPasswordHistoryManagementConfiguration {

    @Configuration(value = "JdbcPasswordHistoryManagementEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordHistoryManagementEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaPasswordHistoryVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaPasswordHistoryPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JdbcPasswordHistoryEntity.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean passwordHistoryEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaPasswordHistoryVendorAdapter")
            final JpaVendorAdapter jpaPasswordHistoryVendorAdapter,
            @Qualifier("jpaPasswordHistoryPackagesToScan")
            final BeanContainer<String> jpaPasswordHistoryPackagesToScan,
            @Qualifier("jdbcPasswordManagementDataSource")
            final DataSource jdbcPasswordManagementDataSource,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx =
                JpaConfigurationContext.builder().jpaVendorAdapter(jpaPasswordHistoryVendorAdapter)
                    .persistenceUnitName("jpaPasswordHistoryContext").dataSource(jdbcPasswordManagementDataSource)
                    .packagesToScan(jpaPasswordHistoryPackagesToScan.toSet()).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getPm().getJdbc());
        }
    }

    @Configuration(value = "JdbcPasswordHistoryManagementTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordHistoryManagementTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerPasswordHistory(
            @Qualifier("passwordHistoryEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }

    @Configuration(value = "JdbcPasswordHistoryManagementServiceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JdbcPasswordHistoryManagementServiceConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public PasswordHistoryService passwordHistoryService() {
            return new JdbcPasswordHistoryService();
        }
    }
}
