package org.apereo.cas.oidc.ticket;

import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.ticket.TicketFactory;

/**
 * This is {@link OidcCibaRequestFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface OidcCibaRequestFactory extends TicketFactory {
    /**
     * Create.
     *
     * @param holder the holder
     * @return the oidc pushed authorization uri
     * @throws Throwable the throwable
     */
    OidcCibaRequest create(CibaRequestContext holder) throws Throwable;

    /**
     * Decode id from request id.
     *
     * @param requestId the request id
     * @return the decoded id
     */
    String decodeId(String requestId);
}
