package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.val;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link U2FInMemoryDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FInMemoryDeviceRepository extends BaseU2FDeviceRepository {

    private final LoadingCache<String, List<U2FDeviceRegistration>> userStorage;

    public U2FInMemoryDeviceRepository(final LoadingCache<String, List<U2FDeviceRegistration>> userStorage,
                                       final LoadingCache<String, String> requestStorage,
                                       final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, cipherExecutor);
        this.userStorage = userStorage;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        return userStorage.asMap().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val values = userStorage.get(username);
        if (values == null) {
            return new ArrayList<>(0);
        }
        return values;
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        val values = userStorage.get(registration.getUsername());
        if (values != null) {
            values.add(registration);
            userStorage.put(registration.getUsername(), values);
        }
        return registration;
    }

    @Override
    public U2FDeviceRegistration verifyRegisteredDevice(final U2FDeviceRegistration registration) {
        val values = userStorage.get(registration.getUsername());
        if (values != null && values.isEmpty()) {
            values.add(registration);
            userStorage.put(registration.getUsername(), values);
        }
        return registration;
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        val values = userStorage.get(username);
        return values != null && !values.isEmpty();
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        val values = userStorage.get(registration.getUsername());
        if (values != null) {
            values.removeIf(r -> r.getId() == registration.getId());
            userStorage.put(registration.getUsername(), values);
        }
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
