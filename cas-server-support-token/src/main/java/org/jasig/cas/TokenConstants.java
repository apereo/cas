package org.jasig.cas;

/**
 * This is {@link TokenConstants}, an interface to hold constants relates to token authentication.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface TokenConstants {
    /** Token parameter passed to the request. */
    String PARAMETER_NAME_TOKEN = "token";

    /** Jwt signing secret defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_SIGNING = "jwtSigningSecret";

    /** Jwt signing secret defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION = "jwtEncryptionSecret";
}

