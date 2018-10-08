package org.apereo.cas.trusted.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustCipherExecutor;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.InMemoryMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.JsonMultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.authentication.storage.MultifactorAuthenticationTrustStorageCleaner;
import org.apereo.cas.trusted.web.MultifactorTrustedDevicesReportEndpoint;
import org.apereo.cas.util.function.FunctionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * This is {@link MultifactorAuthnTrustConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("multifactorAuthnTrustConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreUtilConfiguration.class)
@Slf4j
public class MultifactorAuthnTrustConfiguration implements AuditTrailRecordResolutionPlanConfigurer {
    private static final int INITIAL_CACHE_SIZE = 50;
    private static final long MAX_CACHE_SIZE = 1_000_000;

    @Autowired
    @Qualifier("ticketCreationActionResolver")
    private AuditActionResolver ticketCreationActionResolver;

    @Autowired
    @Qualifier("returnValueResourceResolver")
    private AuditResourceResolver returnValueResourceResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "mfaTrustEngine")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrustStorage mfaTrustEngine() {
        val trusted = casProperties.getAuthn().getMfa().getTrusted();
        final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(trusted.getExpiration(), trusted.getTimeUnit())
            .build(s -> {
                LOGGER.error("Load operation of the cache is not supported.");
                return null;
            });

        storage.asMap();

        val m = FunctionUtils.doIf(trusted.getJson().getLocation() != null,
            () -> {
                LOGGER.debug("Storing trusted device records inside the JSON resource [{}]", trusted.getJson().getLocation());
                return new JsonMultifactorAuthenticationTrustStorage(trusted.getJson().getLocation());
            },
            () -> {
                LOGGER.warn("Storing trusted device records in runtime memory. Changes and records will be lost upon CAS restarts");
                return new InMemoryMultifactorAuthenticationTrustStorage(storage);
            }).get();
        m.setCipherExecutor(mfaTrustCipherExecutor());
        return m;
    }

    @ConditionalOnMissingBean(name = "transactionManagerMfaAuthnTrust")
    @Bean
    public PlatformTransactionManager transactionManagerMfaAuthnTrust() {
        return new PseudoPlatformTransactionManager();
    }

    @Bean
    @RefreshScope
    public CipherExecutor mfaTrustCipherExecutor() {
        val crypto = casProperties.getAuthn().getMfa().getTrusted().getCrypto();
        if (crypto.isEnabled()) {
            return new MultifactorAuthenticationTrustCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        }
        LOGGER.info("Multifactor trusted authentication record encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "trusted authentication records for MFA");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "mfaTrustStorageCleaner")
    @Bean
    public MultifactorAuthenticationTrustStorageCleaner mfaTrustStorageCleaner() {
        return new MultifactorAuthenticationTrustStorageCleaner(
            casProperties.getAuthn().getMfa().getTrusted(),
            mfaTrustEngine());
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditResourceResolver("TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER", this.returnValueResourceResolver);
        plan.registerAuditActionResolver("TRUSTED_AUTHENTICATION_ACTION_RESOLVER", this.ticketCreationActionResolver);
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public MultifactorTrustedDevicesReportEndpoint mfaTrustedDevicesReportEndpoint() {
        return new MultifactorTrustedDevicesReportEndpoint(mfaTrustEngine(),
            casProperties.getAuthn().getMfa().getTrusted());
    }

}
