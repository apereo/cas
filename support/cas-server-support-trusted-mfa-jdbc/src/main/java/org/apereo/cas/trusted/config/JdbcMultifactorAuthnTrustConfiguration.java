package org.apereo.cas.trusted.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustRecordEntityFactory;
import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

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
    private ObjectProvider<CipherExecutor> mfaTrustCipherExecutor;

    @Autowired
    @Qualifier("jpaBeanFactory")
    private ObjectProvider<JpaBeanFactory> jpaBeanFactory;

    @Autowired
    @Qualifier("mfaTrustRecordKeyGenerator")
    private ObjectProvider<MultifactorAuthenticationTrustRecordKeyGenerator> keyGenerationStrategy;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnVendorAdapter")
    public JpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter() {
        return jpaBeanFactory.getObject().newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceMfaTrustedAuthn")
    @RefreshScope
    public DataSource dataSourceMfaTrustedAuthn() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnPackagesToScan")
    public List<String> jpaMfaTrustedAuthnPackagesToScan() {
        val jpa = casProperties.getAuthn().getMfa().getTrusted().getJpa();
        val type = new JpaMultifactorAuthenticationTrustRecordEntityFactory(jpa.getDialect()).getType();
        return CollectionUtils.wrapList(type.getPackage().getName());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean mfaTrustedAuthnEntityManagerFactory() {
        val factory = jpaBeanFactory.getObject();
        val ctx = new JpaConfigurationContext(
            jpaMfaTrustedAuthnVendorAdapter(),
            "jpaMfaTrustedAuthnContext",
            jpaMfaTrustedAuthnPackagesToScan(),
            dataSourceMfaTrustedAuthn());
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerMfaAuthnTrust(
        @Qualifier("mfaTrustedAuthnEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        return new JpaMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(),
            mfaTrustCipherExecutor.getObject(), keyGenerationStrategy.getObject());
    }
}
