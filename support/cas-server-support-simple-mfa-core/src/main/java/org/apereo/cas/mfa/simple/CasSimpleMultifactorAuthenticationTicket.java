package org.apereo.cas.mfa.simple;

import org.apereo.cas.ticket.Ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface CasSimpleMultifactorAuthenticationTicket extends Ticket {
    /**
     * MFA ticket prefix.
     */
    String PREFIX = "CASMFA";
}
