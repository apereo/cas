package org.apereo.cas.config;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.storage.U2FJpaDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.BeanContainer;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link U2FJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "u2fJpaConfiguration", proxyBeanMethods = false)
public class U2FJpaConfiguration {

    @Configuration(value = "U2FJpaTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaTransactionConfiguration {

        @Autowired
        @Bean
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
        @Autowired
        public U2FDeviceRepository u2fDeviceRepository(
            @Qualifier("transactionManagerU2f")
            final PlatformTransactionManager mgr, final CasConfigurationProperties casProperties,
            @Qualifier("u2fRegistrationRecordCipherExecutor")
            final CipherExecutor u2fRegistrationRecordCipherExecutor) {
            val u2f = casProperties.getAuthn().getMfa().getU2f();
            final LoadingCache<String, String> requestStorage =
                Caffeine.newBuilder().expireAfterWrite(u2f.getCore().getExpireRegistrations(),
                    u2f.getCore().getExpireRegistrationsTimeUnit()).build(key -> StringUtils.EMPTY);
            return new U2FJpaDeviceRepository(requestStorage, u2f.getCore().getExpireDevices(),
                u2f.getCore().getExpireDevicesTimeUnit(), u2fRegistrationRecordCipherExecutor);
        }

    }

    @Configuration(value = "U2FJpaEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaU2fVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaU2fPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(U2FDeviceRegistration.class.getPackage().getName()));
        }

        @Bean
        @ConditionalOnMissingBean(name = "u2fEntityManagerFactory")
        @Autowired
        public LocalContainerEntityManagerFactoryBean u2fEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceU2f")
            final DataSource dataSourceU2f,
            @Qualifier("jpaU2fPackagesToScan")
            final BeanContainer<String> jpaU2fPackagesToScan,
            @Qualifier("jpaU2fVendorAdapter")
            final JpaVendorAdapter jpaU2fVendorAdapter,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder().dataSource(dataSourceU2f)
                .packagesToScan(jpaU2fPackagesToScan.toSet()).persistenceUnitName("jpaU2fRegistryContext")
                .jpaVendorAdapter(jpaU2fVendorAdapter).build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getU2f().getJpa());
        }

    }

    @Configuration(value = "U2FJpaDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class U2FJpaDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceU2f")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceU2f(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getU2f().getJpa());
        }
    }
}
