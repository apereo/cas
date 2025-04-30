package org.apereo.cas.ticket.device;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
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

    @Getter
    protected final ExpirationPolicyBuilder<OAuth20DeviceToken> expirationPolicyBuilder;

    protected final int userCodeLength;

    protected final ServicesManager servicesManager;

    @Override
    public OAuth20DeviceToken createDeviceCode(final Service service) throws Throwable {
        val codeId = deviceTokenIdGenerator.getNewTicketId(OAuth20DeviceToken.PREFIX);
        val expirationPolicyToUse = OAuth20DeviceTokenUtils.determineExpirationPolicyForService(servicesManager, expirationPolicyBuilder, service);
        val token = new OAuth20DefaultDeviceToken(codeId, service, expirationPolicyToUse);
        FunctionUtils.doIfNotNull(service, __ -> token.setTenantId(service.getTenant()));
        return token;
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20DeviceToken.class;
    }
}
