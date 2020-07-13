package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.JpaConsentDecision;
import org.apereo.cas.consent.JpaConsentRepository;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.List;

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
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ConsentRepository consentRepository() {
        return new JpaConsentRepository();
    }

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaConsentVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceConsent")
    @RefreshScope
    public DataSource dataSourceConsent() {
        return JpaBeans.newDataSource(casProperties.getConsent().getJpa());
    }

    @Bean
    public List<String> jpaConsentPackagesToScan() {
        return CollectionUtils.wrapList(JpaConsentDecision.class.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean consentEntityManagerFactory() {
        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaConsentVendorAdapter(),
            "jpaConsentContext",
            jpaConsentPackagesToScan(),
            dataSourceConsent());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getConsent().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerConsent(
        @Qualifier("consentEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }
}
