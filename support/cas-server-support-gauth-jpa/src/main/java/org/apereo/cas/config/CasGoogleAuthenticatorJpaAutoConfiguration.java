package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepository;
import org.apereo.cas.gauth.token.JpaGoogleAuthenticatorToken;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.jpa.JpaConfigurationContext;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link CasGoogleAuthenticatorJpaAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.GoogleAuthenticator, module = "jpa")
@AutoConfiguration
public class CasGoogleAuthenticatorJpaAutoConfiguration {

    @Configuration(value = "GoogleAuthenticatorJpaTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorJpaTransactionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "transactionManagerGoogleAuthenticator")
        public PlatformTransactionManager transactionManagerGoogleAuthenticator(
            @Qualifier("googleAuthenticatorEntityManagerFactory")
            final EntityManagerFactory emf) {
            val manager = new JpaTransactionManager();
            manager.setEntityManagerFactory(emf);
            return manager;
        }

    }

    @Configuration(value = "GoogleAuthenticatorJpaRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorJpaRepositoryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaGoogleAuthenticatorAccountRegistry")
        public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
            @Qualifier(CasGoogleAuthenticator.BEAN_NAME)
            final CasGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier("googleAuthenticatorAccountCipherExecutor")
            final CipherExecutor googleAuthenticatorAccountCipherExecutor,
            @Qualifier("googleAuthenticatorScratchCodesCipherExecutor")
            final CipherExecutor googleAuthenticatorScratchCodesCipherExecutor) {
            return new JpaGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorAccountCipherExecutor,
                googleAuthenticatorScratchCodesCipherExecutor, googleAuthenticatorInstance);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(
            final CasConfigurationProperties casProperties,
            @Qualifier("transactionManagerGoogleAuthenticator")
            final PlatformTransactionManager transactionManagerGoogleAuthenticator) {
            val stepSize = casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize();
            val template = new TransactionTemplate(transactionManagerGoogleAuthenticator);
            return new GoogleAuthenticatorJpaTokenRepository(stepSize, template);
        }

    }

    @Configuration(value = "GoogleAuthenticatorJpaDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorJpaDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceGoogleAuthenticator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceGoogleAuthenticator(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getGauth().getJpa());
        }

    }

    @Configuration(value = "GoogleAuthenticatorJpaEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class GoogleAuthenticatorJpaEntityConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jpaPackagesToScanGoogleAuthenticator")
        public BeanContainer<String> jpaPackagesToScanGoogleAuthenticator() {
            return BeanContainer.of(CollectionUtils.wrapSet(GoogleAuthenticatorAccount.class.getPackage().getName(),
                JpaGoogleAuthenticatorToken.class.getPackage().getName()));
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FactoryBean<EntityManagerFactory> googleAuthenticatorEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaGoogleAuthenticatorVendorAdapter")
            final JpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter,
            @Qualifier("dataSourceGoogleAuthenticator")
            final DataSource dataSourceGoogleAuthenticator,
            @Qualifier("jpaPackagesToScanGoogleAuthenticator")
            final BeanContainer<String> jpaPackagesToScanGoogleAuthenticator,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaGoogleAuthenticatorVendorAdapter)
                .persistenceUnitName("jpaGoogleAuthenticatorContext")
                .dataSource(dataSourceGoogleAuthenticator)
                .packagesToScan(jpaPackagesToScanGoogleAuthenticator.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                casProperties.getAuthn().getMfa().getGauth().getJpa());
        }

    }
}
