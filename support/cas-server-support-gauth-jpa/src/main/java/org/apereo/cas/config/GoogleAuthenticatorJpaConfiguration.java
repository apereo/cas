package org.apereo.cas.config;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.JpaGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.adaptors.gauth.JpaGoogleAuthenticatorTokenRepository;
import org.apereo.cas.adaptors.gauth.repository.credentials.GoogleAuthenticatorAccount;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorToken;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
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
@Configuration("googleAuthentiacatorJpaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling
public class GoogleAuthenticatorJpaConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaGoogleAuthenticatorVendorAdapter() {
        return Beans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceGoogleAuthenticator() {
        return Beans.newDataSource(casProperties.getAuthn().getMfa().getGauth().getJpa().getDatabase());
    }

    @Bean
    public String[] jpaPackagesToScanGoogleAuthenticator() {
        return new String[]{GoogleAuthenticatorAccount.class.getPackage().getName(),
                GoogleAuthenticatorToken.class.getPackage().getName()};
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean googleAuthenticatorEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                Beans.newHibernateEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaGoogleAuthenticatorVendorAdapter(),
                                "jpaGoogleAuthenticatorContext",
                                jpaPackagesToScanGoogleAuthenticator(),
                                dataSourceGoogleAuthenticator()),
                        casProperties.getAuthn().getMfa().getGauth().getJpa().getDatabase());

        return bean;
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerGoogleAuthenticator(
            @Qualifier("googleAuthenticatorEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }
    
    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "googleAuthenticatorAccountRegistry")
    public OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry(@Qualifier("googleAuthenticatorInstance")
                                                                               final IGoogleAuthenticator googleAuthenticatorInstance) {
        return new JpaGoogleAuthenticatorTokenCredentialRepository(googleAuthenticatorInstance);
    }

    @ConditionalOnMissingBean(name = "oneTimeTokenAuthenticatorTokenRepository")
    @Bean
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        return new JpaGoogleAuthenticatorTokenRepository(
                casProperties.getAuthn().getMfa().getGauth().getTimeStepSize()
        );
    }

}
