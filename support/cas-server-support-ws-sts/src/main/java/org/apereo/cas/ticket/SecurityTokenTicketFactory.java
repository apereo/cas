package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;

/**
 * This is {@link SecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface SecurityTokenTicketFactory extends TicketFactory {

    /**
     * Create security token ticket.
     *
     * @param ticket        the ticket
     * @param securityToken the security token
     * @return the security token ticket
     */
    SecurityTokenTicket create(TicketGrantingTicket ticket, SecurityToken securityToken);
}
