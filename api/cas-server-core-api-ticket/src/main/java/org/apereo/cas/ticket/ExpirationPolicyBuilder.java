package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Strategy that contains configuration and metadata to construct ticket expiration policies.
 *
 * @author Misagh Moayyed
 * @see Ticket
 * @since 6.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ExpirationPolicyBuilder<T extends Ticket> extends Serializable {

    /**
     * Method build ticket expiration policy.
     *
     * @return - the policy
     */
    ExpirationPolicy buildTicketExpirationPolicy();

    /**
     * Returns the implementation class of the ticket.
     *
     * @return - class implementing the ticket
     */
    Class<T> getTicketType();
}
