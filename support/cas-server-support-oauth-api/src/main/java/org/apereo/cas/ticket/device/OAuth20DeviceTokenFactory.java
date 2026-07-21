package org.apereo.cas.ticket.device;

import module java.base;
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
     * @param scopes  the scopes
     * @return the device token
     * @throws Throwable the throwable
     */
    OAuth20DeviceToken createDeviceCode(Service service, Collection<String> scopes) throws Throwable;
}
