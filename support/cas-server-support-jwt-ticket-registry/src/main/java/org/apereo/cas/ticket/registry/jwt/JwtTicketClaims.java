package org.apereo.cas.ticket.registry.jwt;

/**
 * This is {@link JwtTicketClaims}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface JwtTicketClaims {

    /**
     * The namespace prepended to CAS claims.
     */
    String CAS_NS = "cas";

    /**
     * Ticket content body serialized in the claim.
     */
    String CONTENT_BODY = CAS_NS.concat("-cbd");

    /**
     * Ticket type.
     */
    String TYPE = CAS_NS.concat("-typ");
}
