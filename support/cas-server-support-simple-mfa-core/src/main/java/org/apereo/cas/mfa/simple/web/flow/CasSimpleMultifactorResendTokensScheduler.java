package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.configuration.model.support.mfa.CasSimpleMultifactorProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasSimpleMultifactorResendTokensScheduler}.
 *
 * @author Fotis Memis
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasSimpleMultifactorResendTokensScheduler {

    private final ConcurrentMap<String, ZonedDateTime> principalIdMap;
    private final CasSimpleMultifactorProperties properties;


    @Scheduled(initialDelayString = "${cas.authn.mfa.simple.resendTokenSchedule.start-delay:PT20S}",
            fixedDelayString = "${cas.authn.mfa.simple.resendTokenSchedule.repeat-interval:PT35S}")
    public void run() {
        try {
            LOGGER.info("Beginning simple mfa user map cleanup...");
            this.principalIdMap.entrySet().removeIf(entry -> ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli() > entry.getValue().toInstant().toEpochMilli()
                    + TimeUnit.SECONDS.toMillis(properties.getResendTimeInSeconds()));
            LOGGER.debug("Done decrementing count for simple-mfa throttler.");
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
