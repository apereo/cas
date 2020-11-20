package org.apereo.cas.qr.authentication;

import java.util.List;

/**
 * This is {@link QRAuthenticationDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface QRAuthenticationDeviceRepository {
    /**
     * Permit all qr authentication devices.
     *
     * @return the qr authentication device repository
     */
    static QRAuthenticationDeviceRepository permitAll() {
        return (deviceId, subject) -> true;
    }

    /**
     * Is authorized device for user?.
     *
     * @param deviceId the device id
     * @param subject  the subject
     * @return true /false
     */
    boolean isAuthorizedDeviceFor(String deviceId, String subject);

    /**
     * Authorize device for user.
     *
     * @param deviceId the device id
     * @param subject  the subject
     */
    default void authorizeDeviceFor(final String deviceId, final String subject) {
    }

    /**
     * Remove device.
     *
     * @param device the device
     */
    default void removeDevice(final String device) {
    }

    /**
     * Remove all.
     */
    default void removeAll() {
    }

    /**
     * Gets authorized devices for user.
     *
     * @param subject the subject
     * @return the authorized devices for
     */
    default List<String> getAuthorizedDevicesFor(final String subject) {
        return List.of();
    }
}
