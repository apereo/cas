package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link MultifactorAuthenticationTrustRecordExpiry}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class MultifactorAuthenticationTrustRecordExpiry implements Expiry<String, MultifactorAuthenticationTrustRecord> {

    @Override
    public long expireAfterCreate(
        @NonNull final String key,
        @NonNull final MultifactorAuthenticationTrustRecord value, final long currentTime) {
        if (value.getExpirationDate() == null) {
            LOGGER.trace("Multifactor trust record [{}] will never expire", value);
            return Long.MAX_VALUE;
        }
        if (value.isExpired()) {
            LOGGER.trace("Multifactor trust record [{}] is expired", value);
            return 0;
        }
        try {
            val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
            val zonedExp = DateTimeUtils.zonedDateTimeOf(value.getExpirationDate()).truncatedTo(ChronoUnit.SECONDS);
            val nanos = Duration.between(now, zonedExp).toNanos();
            LOGGER.trace("Multifactor trust record [{}] expires in [{}] nanoseconds", value, nanos);
            return nanos;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        LOGGER.debug("Multifactor trust record [{}] will never expire", value);
        return Long.MAX_VALUE;
    }

    @Override
    public long expireAfterUpdate(
        @NonNull final String key,
        @NonNull final MultifactorAuthenticationTrustRecord value, final long currentTime,
        @NonNegative final long currentDuration) {
        return expireAfterCreate(key, value, currentTime);
    }

    @Override
    public long expireAfterRead(
        @NonNull final String key,
        @NonNull final MultifactorAuthenticationTrustRecord value, final long currentTime,
        @NonNegative final long currentDuration) {
        return expireAfterCreate(key, value, currentTime);
    }
}
