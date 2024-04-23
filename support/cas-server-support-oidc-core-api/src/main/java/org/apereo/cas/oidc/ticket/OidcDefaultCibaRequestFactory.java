package org.apereo.cas.oidc.ticket;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
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

        val authentication = new DefaultAuthenticationBuilder(holder.getPrincipal())
            .addAttribute(OAuth20Constants.SCOPE, holder.getScope())
            .addAttribute(OAuth20Constants.CLIENT_ID, holder.getClientId())
            .addAttribute(OidcConstants.USER_CODE, holder.getUserCode())
            .addAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN, holder.getClientNotificationToken())
            .addAttribute(OidcConstants.BINDING_MESSAGE, holder.getBindingMessage())
            .build();
        return new OidcDefaultCibaRequest(id, authentication, expirationPolicy, holder.getScope(), holder.getClientNotificationToken());
    }
}
