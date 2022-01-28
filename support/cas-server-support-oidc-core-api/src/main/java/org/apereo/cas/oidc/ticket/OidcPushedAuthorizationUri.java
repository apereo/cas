package org.apereo.cas.oidc.ticket;

import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link OidcPushedAuthorizationUri}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OidcPushedAuthorizationUri extends Ticket {
    /**
     * Ticket prefix.
     */
    String PREFIX = "OPAR";
}
