package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.annotation.Nonnull;
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
        @Nonnull
        final String key,
        @Nonnull final MultifactorAuthenticationTrustRecord value, final long currentTime) {
        if (value.getExpirationDate() == null) {
            LOGGER.trace("Multifactor trust record [{}] will never expire", value);
            return Long.MAX_VALUE;
        }
        if (value.isExpired()) {
            LOGGER.trace("Multifactor trust record [{}] is expired", value);
            return 0;
        }
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        val zonedExp = DateTimeUtils.zonedDateTimeOf(value.getExpirationDate()).truncatedTo(ChronoUnit.SECONDS);
        val nanos = Duration.between(now, zonedExp).toNanos();
        LOGGER.trace("Multifactor trust record [{}] expires in [{}] nanoseconds", value, nanos);
        return nanos;
    }

    @Override
    public long expireAfterUpdate(
        @Nonnull final String key,
        @Nonnull final MultifactorAuthenticationTrustRecord value, final long currentTime,
        final long currentDuration) {
        return expireAfterCreate(key, value, currentTime);
    }

    @Override
    public long expireAfterRead(
        @Nonnull final String key,
        @Nonnull final MultifactorAuthenticationTrustRecord value, final long currentTime,
        final long currentDuration) {
        return expireAfterCreate(key, value, currentTime);
    }
}
