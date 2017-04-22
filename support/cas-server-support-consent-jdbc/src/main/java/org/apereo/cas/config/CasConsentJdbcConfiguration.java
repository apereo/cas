package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.JdbcConsentRepository;
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

/**
 * This is {@link CasConsentJdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConsentJdbcConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class CasConsentJdbcConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public ConsentRepository consentRepository() {
        return new JdbcConsentRepository();
    }

    Needs to be reviewed.

    @RefreshScope
    @Bean
    public HibernateJpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter() {
        return Beans.newHibernateJpaVendorAdapter(casProperties.getJdbc());
    }

    @RefreshScope
    @Bean
    public DataSource dataSourceMfaTrustedAuthn() {
        return Beans.newDataSource(casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Bean
    public String[] jpaMfaTrustedAuthnPackagesToScan() {
        return new String[]{"org.apereo.cas.trusted.authentication.api"};
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean mfaTrustedAuthnEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean bean =
                Beans.newHibernateEntityManagerFactoryBean(
                        new JpaConfigDataHolder(
                                jpaMfaTrustedAuthnVendorAdapter(),
                                "jpaMfaTrustedAuthnContext",
                                jpaMfaTrustedAuthnPackagesToScan(),
                                dataSourceMfaTrustedAuthn()),
                        casProperties.getAuthn().getMfa().getTrusted().getJpa());

        bean.getJpaPropertyMap().put("hibernate.enable_lazy_load_no_trans", Boolean.TRUE);
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
}
