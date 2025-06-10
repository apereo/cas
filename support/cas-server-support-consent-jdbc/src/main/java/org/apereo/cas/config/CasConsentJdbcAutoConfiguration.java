package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.JpaConsentDecision;
import org.apereo.cas.consent.JpaConsentRepository;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasConsentJdbcAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Consent, module = "jdbc")
@AutoConfiguration
public class CasConsentJdbcAutoConfiguration {

    @Configuration(value = "CasConsentJdbcRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcRepositoryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConsentRepository consentRepository() {
            return new JpaConsentRepository();
        }
    }

    @Configuration(value = "CasConsentJdbcDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcDataConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "dataSourceConsent")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceConsent(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getConsent().getJpa());
        }

    }

    @Configuration(value = "CasConsentJdbcEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcEntityConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaConsentVendorAdapter(
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaConsentPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaConsentDecision.class.getPackage().getName()));
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public EntityManagerFactory consentEntityManagerFactory(
            @Qualifier("jpaConsentVendorAdapter")
            final JpaVendorAdapter jpaConsentVendorAdapter,
            @Qualifier("dataSourceConsent")
            final DataSource dataSourceConsent,
            @Qualifier("jpaConsentPackagesToScan")
            final BeanContainer<String> jpaConsentPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) throws Exception {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaConsentVendorAdapter)
                .persistenceUnitName("jpaConsentContext")
                .dataSource(dataSourceConsent)
                .packagesToScan(jpaConsentPackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getConsent().getJpa()).getObject();
        }

    }

    @Configuration(value = "CasConsentJdbcTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasConsentJdbcTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerConsent(
            @Qualifier("consentEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }
    }
}
