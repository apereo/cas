package org.apereo.cas.ticket.device;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;
import lombok.val;

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
     * @param scopes the scopes
     * @return the device token
     * @throws Throwable the throwable
     */
    OAuth20DeviceToken createDeviceCode(Service service, Collection<String> scopes) throws Throwable;

    /**
     * Create a device token for a client.
     *
     * @param service  the service
     * @param scopes   the scopes
     * @param clientId the client identifier
     * @return the device token
     * @throws Throwable the throwable
     */
    default OAuth20DeviceToken createDeviceCode(final Service service, final Collection<String> scopes,
                                                final String clientId)
            throws Throwable {
        return createDeviceCode(service, scopes);
    }

    /**
     * Create a device token for a client, carrying the resource indicators of the device authorization request.
     *
     * @param service   the service
     * @param scopes    the scopes
     * @param resources the resource indicators
     * @param clientId  the client identifier
     * @return the device token
     * @throws Throwable the throwable
     */
    default OAuth20DeviceToken createDeviceCode(final Service service, final Collection<String> scopes,
                                                final Collection<String> resources, final String clientId)
            throws Throwable {
        val token = createDeviceCode(service, scopes, clientId);
        token.setResources(resources);
        return token;
    }
}
