package org.apereo.cas.ticket.query;

import org.apereo.cas.ticket.ServiceTicket;

/**
 * This is {@link SamlAttributeQueryTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlAttributeQueryTicket extends ServiceTicket {

    /**
     * The ticket prefix.
     */
    String PREFIX = "SATQ";

    /**
     * Gets relying party.
     *
     * @return the relying party id
     */
    String getRelyingParty();
    
    /**
     * Gets saml object.
     *
     * @return the object
     */
    String getObject();
}
