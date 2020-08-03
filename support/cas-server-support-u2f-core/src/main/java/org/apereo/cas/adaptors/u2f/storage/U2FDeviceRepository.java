package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;

import java.io.Serializable;
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
    Collection<? extends U2FDeviceRegistration> getRegisteredDevices(String username);

    /**
     * Gets registrations.
     *
     * @return the registrations
     */
    Collection<? extends U2FDeviceRegistration> getRegisteredDevices();

    /**
     * Add registration.
     *
     * @param registration the registration
     * @return u2f device registration
     */
    U2FDeviceRegistration registerDevice(U2FDeviceRegistration registration);

    /**
     * Deliver authenticated device upon successful authentication events.
     *
     * @param registration the registration
     * @return u2f device registration
     */
    U2FDeviceRegistration verifyRegisteredDevice(U2FDeviceRegistration registration);

    /**
     * Delete registered device.
     *
     * @param registration the registration
     */
    void deleteRegisteredDevice(U2FDeviceRegistration registration);

    /**
     * Is registered ?
     *
     * @param username the username
     * @return true/false
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

    /**
     * Clean up repository to remove all records.
     *
     * @throws Exception the exception
     */
    void removeAll() throws Exception;

    /**
     * Gets cipher executor.
     *
     * @return the cipher executor
     */
    CipherExecutor<Serializable, String> getCipherExecutor();

    /**
     * Decode u2f device registration.
     *
     * @param registration the registration
     * @return the u2f device registration
     */
    default U2FDeviceRegistration decode(U2FDeviceRegistration registration) {
        val record = registration.clone();
        val data = getCipherExecutor().decode(record.getRecord());
        record.setRecord(data);
        return record;
    }
}
