package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.time.LocalDate;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerU2f")
public class MultifactorAuthenticationTrustStorageCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationTrustStorageCleaner.class);

    private final TrustedDevicesMultifactorProperties trustedProperties;
    private final MultifactorAuthenticationTrustStorage storage;

    public MultifactorAuthenticationTrustStorageCleaner(final TrustedDevicesMultifactorProperties trustedProperties,
                                                        final MultifactorAuthenticationTrustStorage storage) {
        this.trustedProperties = trustedProperties;
        this.storage = storage;
    }

    /**
     * Clean up expired records.
     */
    @Scheduled(initialDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.startDelay:PT10S}",
               fixedDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.repeatInterval:PT60S}")
    public void clean() {

        if (!trustedProperties.getCleaner().getSchedule().isEnabled()) {
            LOGGER.debug("[{}] is disabled. Expired trusted authentication records will not automatically be cleaned up by CAS",
                    getClass().getName());
            return;
        }

        try {
            LOGGER.debug("Proceeding to clean up expired trusted authentication records...");
            
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            final LocalDate validDate = LocalDate.now().minus(trustedProperties.getExpiration(),
                    DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit()));
            LOGGER.info("Expiring records that are on/before [{}]", validDate);
            this.storage.expire(validDate);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
