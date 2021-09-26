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
import java.util.Set;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link JdbcMultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
@Configuration(value = "jdbcMultifactorAuthnTrustConfiguration", proxyBeanMethods = false)
public class JdbcMultifactorAuthnTrustConfiguration {

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnVendorAdapter")
    @Autowired
    public JpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter(final CasConfigurationProperties casProperties, @Qualifier("jpaBeanFactory") final JpaBeanFactory jpaBeanFactory) {
        return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataSourceMfaTrustedAuthn")
    @RefreshScope
    @Autowired
    public DataSource dataSourceMfaTrustedAuthn(final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaMfaTrustedAuthnPackagesToScan")
    @Autowired
    public Set<String> jpaMfaTrustedAuthnPackagesToScan(final CasConfigurationProperties casProperties) {
        val jpa = casProperties.getAuthn().getMfa().getTrusted().getJpa();
        val type = new JpaMultifactorAuthenticationTrustRecordEntityFactory(jpa.getDialect()).getType();
        return CollectionUtils.wrapSet(type.getPackage().getName());
    }

    @Lazy
    @Bean
    @Autowired
    public LocalContainerEntityManagerFactoryBean mfaTrustedAuthnEntityManagerFactory(final CasConfigurationProperties casProperties, @Qualifier("dataSourceMfaTrustedAuthn") final DataSource dataSourceMfaTrustedAuthn, @Qualifier("jpaMfaTrustedAuthnPackagesToScan") final Set<String> jpaMfaTrustedAuthnPackagesToScan, @Qualifier("jpaMfaTrustedAuthnVendorAdapter") final JpaVendorAdapter jpaMfaTrustedAuthnVendorAdapter, @Qualifier("jpaBeanFactory") final JpaBeanFactory jpaBeanFactory) {
        val factory = jpaBeanFactory;
        val ctx = JpaConfigurationContext.builder().dataSource(dataSourceMfaTrustedAuthn).packagesToScan(jpaMfaTrustedAuthnPackagesToScan).persistenceUnitName("jpaMfaTrustedAuthnContext").jpaVendorAdapter(jpaMfaTrustedAuthnVendorAdapter).build();
        return factory.newEntityManagerFactoryBean(ctx, casProperties.getAuthn().getMfa().getTrusted().getJpa());
    }

    @Autowired
    @Bean
    public PlatformTransactionManager transactionManagerMfaAuthnTrust(@Qualifier("mfaTrustedAuthnEntityManagerFactory") final EntityManagerFactory emf) {
        val mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    @Bean
    @Autowired
    public MultifactorAuthenticationTrustStorage mfaTrustEngine(final CasConfigurationProperties casProperties, @Qualifier("mfaTrustCipherExecutor") final CipherExecutor mfaTrustCipherExecutor, @Qualifier("keyGenerationStrategy") final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        return new JpaMultifactorAuthenticationTrustStorage(casProperties.getAuthn().getMfa().getTrusted(), mfaTrustCipherExecutor, keyGenerationStrategy);
    }
}
