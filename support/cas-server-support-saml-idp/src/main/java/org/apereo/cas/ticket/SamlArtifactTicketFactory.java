package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.opensaml.saml.common.SAMLObject;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlArtifactTicketFactory extends TicketFactory {

    /**
     * Create an access token.
     *
     * @param artifactId           the artifact id
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @param issuer               the issuer
     * @param relyingParty         the relying party
     * @param samlObject           the saml object
     * @return the access token
     */
    SamlArtifactTicket create(String artifactId,
                              Authentication authentication,
                              TicketGrantingTicket ticketGrantingTicket,
                              String issuer, String relyingParty,
                              SAMLObject samlObject);

    /**
     * Create ticket id.
     *
     * @param artifactId the artifact id
     * @return the string
     */
    default String createTicketIdFor(final String artifactId) {
        return SamlArtifactTicket.PREFIX + "-" + artifactId;
    }
}
