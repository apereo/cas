package org.apereo.cas.ticket.query;

import org.apereo.cas.ticket.ServiceTicket;

import java.util.Map;

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
     * Gets issuer.
     *
     * @return the issuer
     */
    String getIssuer();
    
    /**
     * Gets saml attributes.
     *
     * @return the object
     */
    Map<String, Object> getAttributes();
}
