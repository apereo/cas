package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationTrustStorageCleaner {
    private final TrustedDevicesMultifactorProperties trustedProperties;
    private final MultifactorAuthenticationTrustStorage storage;

    /**
     * Clean up expired records.
     */
    @Scheduled(initialDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.start-delay:PT10S}",
        fixedDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.repeat-interval:PT60S}")
    public void clean() {
        if (!trustedProperties.getCleaner().getSchedule().isEnabled()) {
            LOGGER.debug("[{}] is disabled; expired trusted authentication records will not be removed automatically", getClass().getName());
        } else {
            try {
                LOGGER.trace("Proceeding to clean up expired trusted authentication records...");
                SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
                this.storage.remove();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
