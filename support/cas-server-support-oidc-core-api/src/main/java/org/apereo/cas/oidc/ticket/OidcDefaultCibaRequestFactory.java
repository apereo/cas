package org.apereo.cas.oidc.ticket;

import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcDefaultCibaRequestFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OidcDefaultCibaRequestFactory implements OidcCibaRequestFactory {
    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator idGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<OidcCibaRequest> expirationPolicyBuilder;

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OidcCibaRequest.class;
    }

    @Override
    public OidcCibaRequest create(final CibaRequestContext holder) throws Throwable {
        val id = idGenerator.getNewTicketId(OidcCibaRequest.PREFIX);
        val expirationPolicy = holder.getRequestedExpiry() > 0
            ? new HardTimeoutExpirationPolicy(holder.getRequestedExpiry())
            : expirationPolicyBuilder.buildTicketExpirationPolicy();
        return new OidcDefaultCibaRequest(id, expirationPolicy, holder.getScope(), holder.getClientNotificationToken());
    }
}
