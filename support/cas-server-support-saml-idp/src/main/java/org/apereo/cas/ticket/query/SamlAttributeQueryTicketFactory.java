package org.apereo.cas.ticket.query;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.opensaml.saml.common.SAMLObject;

import java.util.Map;

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
     * @param attributes           the attributes
     * @param issuer               the issuer
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the access token
     */
    SamlAttributeQueryTicket create(String id, Map<String, Object> attributes, 
                                    String issuer, TicketGrantingTicket ticketGrantingTicket);

    /**
     * Create ticket id.
     *
     * @param id the artifact id
     * @return the string
     */
    default String createTicketIdFor(final String id) {
        return SamlAttributeQueryTicket.PREFIX + "-" + id;
    }
}
