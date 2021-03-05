package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Default OAuth device token factory.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultDeviceTokenFactory implements OAuth20DeviceTokenFactory {
    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator deviceTokenIdGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder<OAuth20DeviceToken> expirationPolicy;

    /**
     * Length of the generated user code.
     */
    protected final int userCodeLength;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    @Override
    public OAuth20DeviceToken createDeviceCode(final Service service) {
        val codeId = deviceTokenIdGenerator.getNewTicketId(OAuth20DeviceToken.PREFIX);
        val expirationPolicyToUse = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager, expirationPolicy, service);
        return new OAuth20DefaultDeviceToken(codeId, service, expirationPolicyToUse);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20DeviceToken.class;
    }
}
