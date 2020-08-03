package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FDynamoDbDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class U2FDynamoDbDeviceRepository extends BaseU2FDeviceRepository {
    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    private final U2FDynamoDbFacilitator facilitator;

    public U2FDynamoDbDeviceRepository(final LoadingCache<String, String> requestStorage,
                                       final CipherExecutor<Serializable, String> cipherExecutor,
                                       final long expirationTime, final TimeUnit expirationTimeUnit,
                                       final U2FDynamoDbFacilitator facilitator) {
        super(requestStorage, cipherExecutor);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.facilitator = facilitator;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            return facilitator.fetchDevicesFrom(expirationDate);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            return facilitator.fetchDevicesFrom(expirationDate, username);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        return facilitator.save(registration);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime,
                DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
            facilitator.removeDevicesBefore(expirationDate);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void removeAll() {
        try {
            facilitator.removeDevices();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration record) {
        try {
            facilitator.removeDevice(record.getUsername(), record.getId());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
