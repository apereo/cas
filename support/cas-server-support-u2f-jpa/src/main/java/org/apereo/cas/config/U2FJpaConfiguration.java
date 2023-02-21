package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJpaDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
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
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link U2FJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.U2F, module = "jpa")
@AutoConfiguration
public class U2FJpaConfiguration {

    @Configuration(value = "U2FJpaTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaTransactionConfiguration {

        @ConditionalOnMissingBean(name = "u2fTransactionTemplate")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TransactionOperations u2fTransactionTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier("transactionManagerU2f")
            final PlatformTransactionManager transactionManagerU2f) {
            val template = new TransactionTemplate(transactionManagerU2f);
            template.setIsolationLevelName(casProperties.getAuthn().getMfa().getU2f().getJpa().getIsolationLevelName());
            template.setPropagationBehaviorName(casProperties.getAuthn().getMfa().getU2f().getJpa().getPropagationBehaviorName());
            return template;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PlatformTransactionManager transactionManagerU2f(
            @Qualifier("u2fEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }

    @Configuration(value = "U2FJpaRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaRepositoryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public U2FDeviceRepository u2fDeviceRepository(
            @Qualifier("u2fTransactionTemplate")
            final TransactionOperations u2fTransactionTemplate,
            final CasConfigurationProperties casProperties,
            @Qualifier("u2fRegistrationRecordCipherExecutor")
            final CipherExecutor u2fRegistrationRecordCipherExecutor) {
            val u2f = casProperties.getAuthn().getMfa().getU2f();
            final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder().expireAfterWrite(u2f.getCore().getExpireRegistrations(),
                    u2f.getCore().getExpireRegistrationsTimeUnit()).build(key -> StringUtils.EMPTY);
            return new U2FJpaDeviceRepository(requestStorage, casProperties,
                u2fRegistrationRecordCipherExecutor, u2fTransactionTemplate);
        }

    }

    @Configuration(value = "U2FJpaEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaU2fVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaU2fPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(U2FDeviceRegistration.class.getPackage().getName()));
        }

        @Bean
        @ConditionalOnMissingBean(name = "u2fEntityManagerFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FactoryBean<EntityManagerFactory> u2fEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceU2f")
            final DataSource dataSourceU2f,
            @Qualifier("jpaU2fPackagesToScan")
            final BeanContainer<String> jpaU2fPackagesToScan,
            @Qualifier("jpaU2fVendorAdapter")
            final JpaVendorAdapter jpaU2fVendorAdapter,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .dataSource(dataSourceU2f)
                .packagesToScan(jpaU2fPackagesToScan.toSet())
                .persistenceUnitName("jpaU2fRegistryContext")
                .jpaVendorAdapter(jpaU2fVendorAdapter).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                casProperties.getAuthn().getMfa().getU2f().getJpa());
        }

    }

    @Configuration(value = "U2FJpaDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceU2f")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceU2f(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getU2f().getJpa());
        }
    }
}
