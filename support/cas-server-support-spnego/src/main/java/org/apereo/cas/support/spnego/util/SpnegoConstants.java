package org.apereo.cas.support.spnego.util;

/**
 * Spnego Constants.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
public interface SpnegoConstants {

    /** The header authenticate. */
    String HEADER_AUTHENTICATE = "WWW-Authenticate";

    /** The header authorization. */
    String HEADER_AUTHORIZATION = "Authorization";

    /** The negotiate. */
    String NEGOTIATE = "Negotiate";

    /** The spnego first time. */
    String SPNEGO_FIRST_TIME = "spnegoFirstTime";

    /** The spnego credentials. */
    String SPNEGO_CREDENTIALS = "spnegoCredentials";

    /** The ntlm. */
    String NTLM = "NTLM";
}
