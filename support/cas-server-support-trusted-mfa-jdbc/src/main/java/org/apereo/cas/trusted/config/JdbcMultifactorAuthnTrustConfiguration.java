package org.apereo.cas.trusted.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;

/**
 * This is {@link JdbcMultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jdbcMultifactorAuthnTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class JdbcMultifactorAuthnTrustConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("mfaTrustCipherExecutor")
    private CipherExecutor mfaTrustCipherExecutor;
    
    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter() {
        return JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceMfaTrustedAuthn() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Bean
    public List<String> jpaMfaTrustedAuthnPackagesToScan() {
        return CollectionUtils.wrapList("org.apereo.cas.trusted.authentication.api");
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean mfaTrustedAuthnEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                JpaBeans.newHibernateEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaMfaTrustedAuthnVendorAdapter(),
                                "jpaMfaTrustedAuthnContext",
                                jpaMfaTrustedAuthnPackagesToScan(),
                                dataSourceMfaTrustedAuthn()),
                        casProperties.getAuthn().getMfa().getTrusted().getJpa());

        return bean;
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerMfaAuthnTrust(
            @Qualifier("mfaTrustedAuthnEntityManagerFactory") final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        final JpaMultifactorAuthenticationTrustStorage m = new JpaMultifactorAuthenticationTrustStorage();
        m.setCipherExecutor(this.mfaTrustCipherExecutor);
        return m;
    }
}
