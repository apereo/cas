package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
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
@Transactional(readOnly = false, transactionManager = "transactionManagerMfaAuthnTrust")
public class MultifactorAuthenticationTrustStorageCleaner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTicketRegistryCleaner.class);

    private MultifactorAuthenticationProperties.Trusted trustedProperties;

    private MultifactorAuthenticationTrustStorage storage;

    /**
     * Clean up expired records.
     */
    @Scheduled(initialDelayString = "${cas.authn.mfa.trusted.cleaner.startDelay:10000}",
               fixedDelayString = "${cas.authn.mfa.trusted.cleaner.repeatInterval:60000}")
    public void clean() {

        if (!trustedProperties.getCleaner().isEnabled()) {
            LOGGER.debug("{} is disabled. Expired records will not automatically be cleaned up by CAS");
            return;
        }

        try {
            LOGGER.debug("Proceeding to clean up expired trusted authentication records...");
            
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
            final LocalDate validDate = LocalDate.now().minus(trustedProperties.getExpiration(),
                    DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit()));
            LOGGER.info("Expiring records that are on/before {}", validDate);
            this.storage.expire(validDate);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void setTrustedProperties(final MultifactorAuthenticationProperties.Trusted trustedProperties) {
        this.trustedProperties = trustedProperties;
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }

}
