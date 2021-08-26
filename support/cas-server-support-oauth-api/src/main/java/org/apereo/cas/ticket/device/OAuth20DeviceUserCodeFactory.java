package org.apereo.cas.ticket.device;

import org.apereo.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth device tokens.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface OAuth20DeviceUserCodeFactory extends TicketFactory {

    /**
     * Create device user code device user code.
     *
     * @param deviceCode the device code
     * @return the device user code
     */
    OAuth20DeviceUserCode createDeviceUserCode(OAuth20DeviceToken deviceCode);

    /**
     * Generate device user code string.
     *
     * @param providedCode the provided code
     * @return the string
     */
    String generateDeviceUserCode(String providedCode);
}
