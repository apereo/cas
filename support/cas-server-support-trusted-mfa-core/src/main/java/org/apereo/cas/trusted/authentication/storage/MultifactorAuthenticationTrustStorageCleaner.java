package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.thread.Cleanable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleaner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = false)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationTrustStorageCleaner implements Cleanable {
    private final MultifactorAuthenticationTrustStorage storage;

    @Override
    @Scheduled(
        cron = "${cas.authn.mfa.trusted.cleaner.schedule.cron-expression:}",
        zone = "${cas.authn.mfa.trusted.cleaner.schedule.cron-time-zone:}",
        initialDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.start-delay:PT10S}",
        fixedDelayString = "${cas.authn.mfa.trusted.cleaner.schedule.repeat-interval:PT60S}")
    public void clean() {
        FunctionUtils.doAndHandle(_ -> {
            LOGGER.trace("Proceeding to clean up expired trusted authentication records...");
            storage.remove();
        });
    }
}
