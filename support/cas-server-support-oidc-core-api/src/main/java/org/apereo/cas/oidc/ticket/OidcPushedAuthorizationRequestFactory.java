package org.apereo.cas.oidc.ticket;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.TicketFactory;

/**
 * This is {@link OidcPushedAuthorizationRequestFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface OidcPushedAuthorizationRequestFactory extends TicketFactory {
    /**
     * Create.
     *
     * @param holder the holder
     * @return the oidc pushed authorization uri
     * @throws Exception the exception
     */
    OidcPushedAuthorizationRequest create(AccessTokenRequestDataHolder holder) throws Exception;
}
