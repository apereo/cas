package org.apereo.cas.oidc.ticket;

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
     * @throws Exception the exception
     */
    OidcPushedAuthorizationRequest create(AccessTokenRequestContext holder) throws Exception;

    /**
     * To access token request access token request data holder.
     *
     * @param authzRequest the authz request
     * @return the access token request data holder
     * @throws Exception the exception
     */
    AccessTokenRequestContext toAccessTokenRequest(OidcPushedAuthorizationRequest authzRequest) throws Exception;
}
