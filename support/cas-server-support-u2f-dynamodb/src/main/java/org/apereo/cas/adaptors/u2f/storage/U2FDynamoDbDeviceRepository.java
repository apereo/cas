package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.util.Collection;

/**
 * This is {@link U2FDynamoDbDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class U2FDynamoDbDeviceRepository extends BaseU2FDeviceRepository {
    private final U2FDynamoDbFacilitator facilitator;

    public U2FDynamoDbDeviceRepository(final LoadingCache<String, String> requestStorage,
                                       final CipherExecutor<Serializable, String> cipherExecutor,
                                       final CasConfigurationProperties casProperties,
                                       final U2FDynamoDbFacilitator facilitator) {
        super(casProperties, requestStorage, cipherExecutor);
        this.facilitator = facilitator;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val expirationDate = getDeviceExpiration();
        return facilitator.fetchDevicesFrom(expirationDate, username);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        val expirationDate = getDeviceExpiration();
        return facilitator.fetchDevicesFrom(expirationDate);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        return facilitator.save(registration);
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration record) {
        facilitator.removeDevice(record.getUsername(), record.getId());
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        val expirationDate = getDeviceExpiration();
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
        facilitator.removeDevicesBefore(expirationDate);
    }

    @Override
    public void removeAll() {
        facilitator.removeDevices();
    }
}
