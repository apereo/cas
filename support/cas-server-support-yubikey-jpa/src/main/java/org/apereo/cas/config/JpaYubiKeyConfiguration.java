package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.JpaYubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.dao.JpaYubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
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
 * This is {@link JpaYubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "jpaYubiKeyConfiguration", proxyBeanMethods = false)
public class JpaYubiKeyConfiguration {


    @Configuration(value = "JpaYubiKeyEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaYubiKeyEntityConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaYubiKeyVendorAdapter(final CasConfigurationProperties casProperties,
                                                        @Qualifier("jpaBeanFactory")
                                                        final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaYubiKeyPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(JpaYubiKeyAccount.class.getPackage().getName()));
        }

        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean yubiKeyEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("dataSourceYubiKey")
            final DataSource dataSourceYubiKey,
            @Qualifier("jpaYubiKeyPackagesToScan")
            final BeanContainer<String> jpaYubiKeyPackagesToScan,
            @Qualifier("jpaYubiKeyVendorAdapter")
            final JpaVendorAdapter jpaYubiKeyVendorAdapter,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .dataSource(dataSourceYubiKey)
                .packagesToScan(jpaYubiKeyPackagesToScan.toSet())
                .persistenceUnitName("jpaYubiKeyRegistryContext")
                .jpaVendorAdapter(jpaYubiKeyVendorAdapter)
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getYubikey().getJpa());
        }

    }

    @Configuration(value = "JpaYubiKeyTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaYubiKeyTransactionConfiguration {
        @Autowired
        @Bean
        public PlatformTransactionManager transactionManagerYubiKey(
            @Qualifier("yubiKeyEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }


    @Configuration(value = "JpaYubiKeyRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaYubiKeyRegistryConfiguration {
        @Bean
        public YubiKeyAccountRegistry yubiKeyAccountRegistry(
            @Qualifier("yubiKeyAccountValidator")
            final YubiKeyAccountValidator yubiKeyAccountValidator,
            @Qualifier("yubikeyAccountCipherExecutor")
            final CipherExecutor yubikeyAccountCipherExecutor) {
            val registry = new JpaYubiKeyAccountRegistry(yubiKeyAccountValidator);
            registry.setCipherExecutor(yubikeyAccountCipherExecutor);
            return registry;
        }
    }


    @Configuration(value = "JpaYubiKeyDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class JpaYubiKeyDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceYubiKey")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceYubiKey(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getYubikey().getJpa());
        }

    }
}
