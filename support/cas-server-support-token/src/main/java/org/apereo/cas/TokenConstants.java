package org.apereo.cas;

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
    
    /** Jwt signing secret alg defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_SIGNING_ALG = "jwtSigningSecretAlg";

    /** Jwt encryption secret defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION = "jwtEncryptionSecret";

    /** Jwt encryption secret alg defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION_ALG = "jwtEncryptionSecretAlg";

    /** Jwt encryption secret method defined for a given service. **/
    String PROPERTY_NAME_TOKEN_SECRET_ENCRYPTION_METHOD = "jwtEncryptionSecretMethod";
}

