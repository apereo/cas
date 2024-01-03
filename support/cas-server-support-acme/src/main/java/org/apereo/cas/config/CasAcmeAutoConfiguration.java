package org.apereo.cas.config;

import org.apereo.cas.acme.AcmeAuthorizationExecutor;
import org.apereo.cas.acme.AcmeCertificateManager;
import org.apereo.cas.acme.AcmeChallengeRepository;
import org.apereo.cas.acme.AcmeWellKnownChallengeController;
import org.apereo.cas.acme.DefaultAcmeCertificateManager;
import org.apereo.cas.acme.DefaultAcmeChallengeRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import java.security.Security;

/**
 * This is {@link CasAcmeAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ACME)
@AutoConfiguration
public class CasAcmeAutoConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AcmeWellKnownChallengeController acmeWellKnownChallengeController(
        @Qualifier("acmeChallengeRepository")
        final AcmeChallengeRepository acmeChallengeRepository) {
        return new AcmeWellKnownChallengeController(acmeChallengeRepository);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "acmeChallengeRepository")
    public AcmeChallengeRepository acmeChallengeRepository() {
        return new DefaultAcmeChallengeRepository();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "acmeAuthorizationExecutor")
    public AcmeAuthorizationExecutor acmeAuthorizationExecutor() {
        return AcmeAuthorizationExecutor.defaultChallenge();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = AcmeCertificateManager.BEAN_NAME)
    public AcmeCertificateManager acmeCertificateManager(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("acmeChallengeRepository")
        final AcmeChallengeRepository acmeChallengeRepository,
        @Qualifier("acmeAuthorizationExecutor")
        final AcmeAuthorizationExecutor acmeAuthorizationExecutor) throws Exception {
        return BeanSupplier.of(AcmeCertificateManager.class)
            .when(BeanCondition.on("cas.acme.terms-of-use-accepted").isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultAcmeCertificateManager(acmeChallengeRepository, casProperties, acmeAuthorizationExecutor))
            .otherwiseProxy()
            .get();
    }

    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) throws Exception {
        val casProperties = event.getApplicationContext().getBean(CasConfigurationProperties.class);
        val domains = casProperties.getAcme().getDomains();
        LOGGER.info("Fetching certificates for domains [{}]", domains);
        if (event.getApplicationContext().containsBean(AcmeCertificateManager.BEAN_NAME)) {
            val acmeCertificateManager = event.getApplicationContext().getBean(AcmeCertificateManager.BEAN_NAME, AcmeCertificateManager.class);
            acmeCertificateManager.fetchCertificate(domains);
        }
    }
}
