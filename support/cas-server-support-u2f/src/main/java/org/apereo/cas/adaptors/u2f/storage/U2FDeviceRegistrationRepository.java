package org.apereo.cas.adaptors.u2f.storage;

import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;

import java.util.List;
import java.util.Map;

/**
 * This is {@link U2FDeviceRegistrationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface U2FDeviceRegistrationRepository {

    /**
     * Gets request storage.
     *
     * @return the request storage
     */
    Map<String, String> getRequestStorage();

    /**
     * Gets user storage.
     *
     * @return the user storage
     */
    LoadingCache<String, Map<String, String>> getUserStorage();

    /**
     * Gets registrations.
     *
     * @param username the username
     * @return the registrations
     */
    List<DeviceRegistration> getRegistrations(String username);

    /**
     * Add registration.
     *
     * @param username     the username
     * @param registration the registration
     */
    void addRegistration(String username, DeviceRegistration registration);

    /**
     * Is registered ?
     *
     * @param username the username
     * @return the boolean
     */
    boolean isRegistered(String username);
}
