package org.apereo.cas.adaptors.u2f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link U2FDeviceRegistrationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FDeviceRegistrationRepository {
    private final Map<String, String> requestStorage = new HashMap<String, String>();
    private final LoadingCache<String, Map<String, String>> userStorage =
            CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {
                @Override
                public Map<String, String> load(final String key) throws Exception {
                    return new HashMap<>();
                }
            });

    public Map<String, String> getRequestStorage() {
        return requestStorage;
    }

    public LoadingCache<String, Map<String, String>> getUserStorage() {
        return userStorage;
    }

    /**
     * Gets registrations.
     *
     * @param username the username
     * @return the registrations
     */
    public List<DeviceRegistration> getRegistrations(final String username) {
        final List<DeviceRegistration> registrations = new ArrayList<>();
        for (final String serialized : userStorage.getUnchecked(username).values()) {
            registrations.add(DeviceRegistration.fromJson(serialized));
        }
        return registrations;
    }

    /**
     * Add registration.
     *
     * @param username     the username
     * @param registration the registration
     */
    public void addRegistration(final String username, final DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }
}
