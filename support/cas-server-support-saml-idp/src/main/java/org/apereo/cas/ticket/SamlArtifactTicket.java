package org.apereo.cas.ticket;

/**
 * This is {@link SamlArtifactTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlArtifactTicket extends ServiceTicket {

    /**
     * The ticket prefix.
     */
    String PREFIX = "SART";

    /**
     * Gets issuer.
     *
     * @return the issuer
     */
    String getIssuer();

    /**
     * Relying party id.
     *
     * @return the string
     */
    String getRelyingPartyId();
    

    /**
     * Gets saml object.
     *
     * @return the object
     */
    String getObject();
}
