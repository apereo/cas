package org.apereo.cas.authentication.device;

import org.apereo.cas.authentication.principal.Principal;
import java.util.List;

/**
 * This is {@link MultifactorAuthenticationDeviceManager}.
 * A general abstraction for each multifactor provider to determine
 * if a given user has registered devices and optionally fetch those devices.
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface MultifactorAuthenticationDeviceManager {

    /**
     * Gets source for accounts and devices.
     *
     * @return the source
     */
    List<String> getSource();
    
    /**
     * Has registered devices for a given provider.
     *
     * @param principal the principal
     * @return true/false
     */
    default boolean hasRegisteredDevices(final Principal principal) {
        return !findRegisteredDevices(principal).isEmpty();
    }

    /**
     * Find registered devices list.
     *
     * @param principal the principal
     * @return the list
     */
    List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(Principal principal);

    /**
     * Remove registered device.
     *
     * @param principal the principal
     * @param deviceId  the device id
     */
    default void removeRegisteredDevice(final Principal principal, final String deviceId) {
    }
}
