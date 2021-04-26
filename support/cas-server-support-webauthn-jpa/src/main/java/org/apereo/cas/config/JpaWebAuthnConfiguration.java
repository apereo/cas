package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.JpaWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Set;

/**
 * This is {@link JpaWebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("JpaWebAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaWebAuthnConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    @Qualifier("webAuthnCredentialRegistrationCipherExecutor")
    private ObjectProvider<CipherExecutor> webAuthnCredentialRegistrationCipherExecutor;

    @RefreshScope
    @Bean
    public JpaVendorAdapter jpaWebAuthnVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceWebAuthn")
    @RefreshScope
    public DataSource dataSourceWebAuthn() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getWebAuthn().getJpa());
    }

    @Bean
    public Set<String> jpaWebAuthnPackagesToScan() {
        return CollectionUtils.wrapSet(JpaWebAuthnCredentialRegistration.class.getPackage().getName());
    }

    @Lazy
    @Bean
    @ConditionalOnMissingBean(name = "webAuthnEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean webAuthnEntityManagerFactory() {
        val factory = jpaBeanFactory.getObject();
        val ctx = JpaConfigurationContext.builder()
            .dataSource(dataSourceWebAuthn())
            .packagesToScan(jpaWebAuthnPackagesToScan())
            .persistenceUnitName("jpaWebAuthnRegistryContext")
            .jpaVendorAdapter(jpaWebAuthnVendorAdapter())
            .build();
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getWebAuthn().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerWebAuthn(
        @Qualifier("webAuthnEntityManagerFactory")
        final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }


    @RefreshScope
    @Bean
    public WebAuthnCredentialRepository webAuthnCredentialRepository(
        @Qualifier("transactionManagerWebAuthn")
        final PlatformTransactionManager transactionManager) {
        return new JpaWebAuthnCredentialRepository(casProperties,
            webAuthnCredentialRegistrationCipherExecutor.getObject(), transactionManager);
    }
}
