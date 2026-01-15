package org.apereo.cas.ticket;

import module java.base;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link SecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface SecurityTokenTicketFactory extends TicketFactory {

    /**
     * Create security token ticket.
     *
     * @param ticket                  the ticket
     * @param securityTokenSerialized the security token
     * @return the security token ticket
     * @throws Throwable the throwable
     */
    SecurityTokenTicket create(TicketGrantingTicket ticket, byte[] securityTokenSerialized) throws Throwable;
}
