package org.apereo.cas.adaptors.u2f.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * This is {@link BaseU2FDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseU2FDeviceRepository implements U2FDeviceRepository {
    private final LoadingCache<String, String> requestStorage;

    public BaseU2FDeviceRepository(final LoadingCache<String, String> requestStorage) {
        this.requestStorage = requestStorage;
    }

    @Override
    public String getDeviceRegistrationRequest(final String requestId, final String username) {
        final String request = requestStorage.get(requestId);
        requestStorage.invalidate(requestId);
        requestStorage.cleanUp();
        return request;
    }

    @Override
    public String getDeviceAuthenticationRequest(final String requestId, final String username) {
        final String request = requestStorage.get(requestId);
        requestStorage.invalidate(requestId);
        requestStorage.cleanUp();
        return request;
    }

    @Override
    public void requestDeviceRegistration(final String requestId, final String username, final String registrationJsonData) {
        requestStorage.put(requestId, registrationJsonData);
    }

    @Override
    public void requestDeviceAuthentication(final String requestId, final String username, final String registrationJsonData) {
        requestStorage.put(requestId, registrationJsonData);
    }
}
