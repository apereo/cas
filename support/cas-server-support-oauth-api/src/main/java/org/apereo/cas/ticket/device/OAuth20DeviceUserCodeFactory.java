package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth device tokens.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface OAuth20DeviceUserCodeFactory extends TicketFactory {

    /**
     * Create OAuth 2.0 device user code.
     *
     * @param service the service
     * @return the device user code
     */
    default OAuth20DeviceUserCode createDeviceUserCode(final Service service) {
        return createDeviceUserCode(null, service);
    }

    /**
     * Create OAuth 2.0 device user code.
     *
     * @param id      the id
     * @param service the service
     * @return the OAuth device user code
     */
    OAuth20DeviceUserCode createDeviceUserCode(String id, Service service);

    /**
     * Generate device user code string.
     *
     * @param providedCode the provided code
     * @return the string
     */
    String normalizeUserCode(String providedCode);
}
