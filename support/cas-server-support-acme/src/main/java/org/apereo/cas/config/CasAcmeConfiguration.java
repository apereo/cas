package org.apereo.cas.config;

import org.apereo.cas.acme.AcmeAuthorizationExecutor;
import org.apereo.cas.acme.AcmeCertificateManager;
import org.apereo.cas.acme.AcmeChallengeRepository;
import org.apereo.cas.acme.AcmeWellKnownChallengeController;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.security.Security;

/**
 * This is {@link CasAcmeConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@Configuration(value = "CasAcmeConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.acme.server-url")
public class CasAcmeConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public AcmeWellKnownChallengeController acmeWellKnownChallengeController(
        @Qualifier("acmeChallengeRepository")
        final AcmeChallengeRepository acmeChallengeRepository) {
        return new AcmeWellKnownChallengeController(acmeChallengeRepository);
    }

    @Bean
    public AcmeChallengeRepository acmeChallengeRepository() {
        return new AcmeChallengeRepository();
    }

    @Bean
    @ConditionalOnMissingBean(name = "acmeAuthorizationExecutor")
    public AcmeAuthorizationExecutor acmeAuthorizationExecutor() {
        return AcmeAuthorizationExecutor.defaultChallenge();
    }

    @Bean
    @ConditionalOnProperty(prefix = "cas.acme", name = "terms-of-use-accepted", havingValue = "true")
    @Autowired
    public AcmeCertificateManager acmeCertificateManager(final CasConfigurationProperties casProperties,
                                                         @Qualifier("acmeChallengeRepository")
                                                         final AcmeChallengeRepository acmeChallengeRepository,
                                                         @Qualifier("acmeAuthorizationExecutor")
                                                         final AcmeAuthorizationExecutor acmeAuthorizationExecutor) {
        return new AcmeCertificateManager(acmeChallengeRepository, casProperties, acmeAuthorizationExecutor);
    }


    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) throws Exception {
        val casProperties = event.getApplicationContext().getBean(CasConfigurationProperties.class);
        val domains = casProperties.getAcme().getDomains();
        LOGGER.info("Fetching certificates for domains [{}]", domains);
        if (event.getApplicationContext().containsBean("acmeCertificateManager")) {
            val acmeCertificateManager = event.getApplicationContext().getBean("acmeCertificateManager", AcmeCertificateManager.class);
            acmeCertificateManager.fetchCertificate(domains);
        }
    }
}
