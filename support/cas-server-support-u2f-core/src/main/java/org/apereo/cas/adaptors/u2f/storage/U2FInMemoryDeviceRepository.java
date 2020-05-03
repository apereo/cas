package org.apereo.cas.adaptors.u2f.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link U2FInMemoryDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class U2FInMemoryDeviceRepository extends BaseU2FDeviceRepository {

    private final LoadingCache<String, Map<String, String>> userStorage;

    public U2FInMemoryDeviceRepository(final LoadingCache<String, Map<String, String>> userStorage,
                                       final LoadingCache<String, String> requestStorage) {
        super(requestStorage);
        this.userStorage = userStorage;
    }

    @Override
    @SneakyThrows
    public Collection<? extends DeviceRegistration> getRegisteredDevices(final String username) {
        val values = userStorage.get(username);
        if (values == null) {
            return new ArrayList<>(0);
        }

        return values.values()
            .stream()
            .map(r -> {
                try {
                    return DeviceRegistration.fromJson(r);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        val values = userStorage.get(username);
        if (values != null) {
            values.put(registration.getKeyHandle(), registration.toJsonWithAttestationCert());
        }
    }

    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        val values = userStorage.get(username);
        if (values != null) {
            values.put(registration.getKeyHandle(), registration.toJsonWithAttestationCert());
        }
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        val values = userStorage.get(username);
        return values != null && !values.values().isEmpty();
    }

    @Override
    public void clean() {
        this.userStorage.cleanUp();
    }

    @Override
    public void removeAll() {
        userStorage.invalidateAll();
    }
}
