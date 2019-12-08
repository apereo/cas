package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth device tokens.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OAuth20DeviceTokenFactory extends TicketFactory {

    /**
     * Create an device token.
     *
     * @param service the service
     * @return the device token
     */
    OAuth20DeviceToken createDeviceCode(Service service);

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
