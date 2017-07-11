package org.apereo.cas.adaptors.u2f.storage;

import com.yubico.u2f.data.DeviceRegistration;

import java.util.Collection;

/**
 * This is {@link U2FDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface U2FDeviceRepository {

    /**
     * Gets registrations.
     *
     * @param username the username
     * @return the registrations
     */
    Collection<DeviceRegistration> getRegisteredDevices(String username);

    /**
     * Add registration.
     *
     * @param username     the username
     * @param registration the registration
     */
    void registerDevice(String username, DeviceRegistration registration);

    /**
     * Deliver authenticated device upon successful authentication events.
     *
     * @param username     the username
     * @param registration the registration
     */
    void authenticateDevice(String username, DeviceRegistration registration);

    /**
     * Is registered ?
     *
     * @param username the username
     * @return the boolean
     */
    boolean isDeviceRegisteredFor(String username);

    /**
     * Gets device registration request.
     *
     * @param requestId the request id
     * @param username  the username
     * @return the device registration request in JSON
     */
    String getDeviceRegistrationRequest(String requestId, String username);

    /**
     * Gets device authentication request.
     *
     * @param requestId the request id
     * @param username  the username
     * @return the device authentication request
     */
    String getDeviceAuthenticationRequest(String requestId, String username);

    /**
     * Request device registration.
     *
     * @param requestId            the request id
     * @param username             the username
     * @param registrationJsonData the registration json data
     */
    void requestDeviceRegistration(String requestId, String username, String registrationJsonData);

    /**
     * Request device authentication.
     *
     * @param requestId            the request id
     * @param username             the username
     * @param registrationJsonData the registration json data
     */
    void requestDeviceAuthentication(String requestId, String username, String registrationJsonData);

    /**
     * Clean up repository to remove expired records.
     */
    void clean();
}
