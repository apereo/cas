package org.apereo.cas.adaptors.u2f.storage;

import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link U2FInMemoryDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FInMemoryDeviceRepository extends BaseU2FDeviceRepository {

    private final LoadingCache<String, Map<String, String>> userStorage;

    public U2FInMemoryDeviceRepository(final LoadingCache<String, Map<String, String>> userStorage,
                                       final LoadingCache<String, String> requestStorage) {
        super(requestStorage);
        this.userStorage = userStorage;
    }

    @Override
    public List<DeviceRegistration> getRegisteredDevices(final String username) {
        final List<DeviceRegistration> registrations = userStorage.getUnchecked(username).values()
                .stream().map(DeviceRegistration::fromJson).collect(Collectors.toList());
        return registrations;
    }
    
    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }

    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !userStorage.getUnchecked(username).values().isEmpty();
    }
}
