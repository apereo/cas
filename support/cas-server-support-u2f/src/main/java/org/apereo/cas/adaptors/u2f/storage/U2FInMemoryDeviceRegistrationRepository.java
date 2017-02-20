package org.apereo.cas.adaptors.u2f.storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link U2FInMemoryDeviceRegistrationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FInMemoryDeviceRegistrationRepository implements U2FDeviceRegistrationRepository {
    private final Map<String, String> requestStorage = new HashMap<>();
    
    private final LoadingCache<String, Map<String, String>> userStorage =
            CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {
                @Override
                public Map<String, String> load(final String key) throws Exception {
                    return new HashMap<>();
                }
            });

    @Override
    public List<DeviceRegistration> getRegistrations(final String username) {
        final List<DeviceRegistration> registrations = userStorage.getUnchecked(username).values()
                .stream().map(DeviceRegistration::fromJson).collect(Collectors.toList());
        return registrations;
    }

 
    @Override
    public void addRegistration(final String username, final DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }

    @Override
    public boolean isRegistered(final String username) {
        return !userStorage.getUnchecked(username).values().isEmpty();
    }

    @Override
    public Map<String, String> getRequestStorage() {
        return this.requestStorage;
    }

    @Override
    public LoadingCache<String, Map<String, String>> getUserStorage() {
        return this.userStorage;
    }
}
