package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
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
     * @throws Throwable the throwable
     */
    OidcPushedAuthorizationRequest create(AccessTokenRequestContext holder) throws Throwable;

    /**
     * To access token request access token request data holder.
     *
     * @param authzRequest the authz request
     * @return the access token request data holder
     */
    AccessTokenRequestContext toAccessTokenRequest(OidcPushedAuthorizationRequest authzRequest);
}
