package org.apereo.cas.ticket.query;

import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;

import org.opensaml.saml.common.SAMLObject;

import jakarta.validation.constraints.NotNull;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlAttributeQueryTicketFactory extends TicketFactory {

    /**
     * Create the ticket.
     *
     * @param id                   the id
     * @param samlObject           the saml object
     * @param relyingParty         the relying party
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the access token
     */
    SamlAttributeQueryTicket create(String id,
                                    @NotNull
                                    SAMLObject samlObject,
                                    @NotNull
                                    String relyingParty,
                                    @NotNull
                                    TicketGrantingTicket ticketGrantingTicket);

    /**
     * Create ticket id.
     *
     * @param id           the artifact id
     * @param relyingParty the relying party
     * @return the string
     */
    default String createTicketIdFor(final String id, final String relyingParty) {
        return SamlAttributeQueryTicket.PREFIX + '-' + id + '-' + relyingParty;
    }
}
