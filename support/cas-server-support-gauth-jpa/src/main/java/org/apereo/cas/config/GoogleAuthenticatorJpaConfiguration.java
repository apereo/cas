package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorJpaTokenRepository;
import org.apereo.cas.gauth.token.JpaGoogleAuthenticatorToken;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.BeanContainer;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * This is {@link GoogleAuthenticatorJpaConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@EnableScheduling
@Configuration(value = "googleAuthentiacatorJpaConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorJpaConfiguration {

    @Configuration(value = "GoogleAuthenticatorJpaTransactionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorJpaTransactionConfiguration {
        @Autowired
        @Bean
        @ConditionalOnMissingBean(name = "transactionManagerGoogleAuthenticator")
        public PlatformTransactionManager transactionManagerGoogleAuthenticator(
            @Qualifier("googleAuthenticatorEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

    }

    @Configuration(value = "GoogleAuthenticatorJpaRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorJpaRepositoryConfiguration {
        @Autowired
        @Bean
        @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
        public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(
            @Qualifier("googleAuthenticatorInstance")
            final IGoogleAuthenticator googleAuthenticatorInstance,
            @Qualifier("googleAuthenticatorAccountCipherExecutor")
            final CipherExecutor googleAuthenticatorAccountCipherExecutor) {
            return new JpaGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorAccountCipherExecutor, googleAuthenticatorInstance);
        }

        @Bean
        @Autowired
        public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository(final CasConfigurationProperties casProperties) {
            return new GoogleAuthenticatorJpaTokenRepository(casProperties.getAuthn().getMfa().getGauth().getCore().getTimeStepSize());
        }

    }
    
    @Configuration(value = "GoogleAuthenticatorJpaDataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorJpaDataConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceGoogleAuthenticator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public DataSource dataSourceGoogleAuthenticator(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getGauth().getJpa());
        }

    }

    @Configuration(value = "GoogleAuthenticatorJpaEntityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class GoogleAuthenticatorJpaEntityConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jpaPackagesToScanGoogleAuthenticator")
        public BeanContainer<String> jpaPackagesToScanGoogleAuthenticator() {
            return BeanContainer.of(CollectionUtils.wrapSet(GoogleAuthenticatorAccount.class.getPackage().getName(),
                JpaGoogleAuthenticatorToken.class.getPackage().getName()));
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public JpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        
        @Bean
        @Autowired
        public LocalContainerEntityManagerFactoryBean googleAuthenticatorEntityManagerFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaGoogleAuthenticatorVendorAdapter")
            final JpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter,
            @Qualifier("dataSourceGoogleAuthenticator")
            final DataSource dataSourceGoogleAuthenticator,
            @Qualifier("jpaPackagesToScanGoogleAuthenticator")
            final BeanContainer<String> jpaPackagesToScanGoogleAuthenticator,
            @Qualifier("jpaBeanFactory")
            final JpaBeanFactory jpaBeanFactory) {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaGoogleAuthenticatorVendorAdapter)
                .persistenceUnitName("jpaGoogleAuthenticatorContext")
                .dataSource(dataSourceGoogleAuthenticator)
                .packagesToScan(jpaPackagesToScanGoogleAuthenticator.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getGauth().getJpa());
        }

    }
}
