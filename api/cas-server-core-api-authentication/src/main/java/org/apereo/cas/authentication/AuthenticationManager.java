package org.apereo.cas.authentication;

/**
 * Authenticates one or more credentials.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface AuthenticationManager {

    /** Authentication method attribute name. **/
    String AUTHENTICATION_METHOD_ATTRIBUTE = "authenticationMethod";

    /**
     * Authenticates the provided credentials. On success, an {@link Authentication} object
     * is returned containing metadata about the result of each authenticated credential.
     * Note that a particular implementation may require some or all credentials to be
     * successfully authenticated. Failure to authenticate is considered an exceptional case, and
     * an AuthenticationException is thrown.
     *
     * @param authenticationTransaction Process a single authentication transaction
     *
     * @return Authentication object on success that contains metadata about credentials that were authenticated.
     *
     * @throws AuthenticationException On authentication failure. The exception contains details
     * on each of the credentials that failed to authenticate.
     */
    Authentication authenticate(AuthenticationTransaction authenticationTransaction) throws AuthenticationException;
}
