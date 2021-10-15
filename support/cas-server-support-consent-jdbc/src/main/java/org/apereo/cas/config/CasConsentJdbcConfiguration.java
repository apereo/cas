package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.JpaConsentDecision;
import org.apereo.cas.consent.JpaConsentRepository;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * This is {@link CasConsentJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casConsentJdbcConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
public class CasConsentJdbcConfiguration {

    @Configuration(value = "CasConsentJdbcRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentJdbcRepositoryConfiguration {
        @Bean
        public ConsentRepository consentRepository() {
            return new JpaConsentRepository();
        }
    }

    @Configuration(value = "CasConsentJdbcDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentJdbcDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceConsent")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceConsent(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getConsent().getJpa());
        }

    }
    
    @Configuration(value = "CasConsentJdbcEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentJdbcEntityConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaConsentVendorAdapter(
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaConsentPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaConsentDecision.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean consentEntityManagerFactory(
            @Qualifier("jpaConsentVendorAdapter")
            final JpaVendorAdapter jpaConsentVendorAdapter,
            @Qualifier("dataSourceConsent")
            final DataSource dataSourceConsent,
            @Qualifier("jpaConsentPackagesToScan")
            final BeanContainer<String> jpaConsentPackagesToScan,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaConsentVendorAdapter)
                .persistenceUnitName("jpaConsentContext")
                .dataSource(dataSourceConsent)
                .packagesToScan(jpaConsentPackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getConsent().getJpa());
        }

    }

    @Configuration(value = "CasConsentJdbcTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentJdbcTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerConsent(
            @Qualifier("consentEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }
    }
}
