package org.apereo.cas.authentication;
import module java.base;

/**
 * Authenticates one or more credentials.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@FunctionalInterface
public interface AuthenticationManager {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "casAuthenticationManager";

    /**
     * Authentication method attribute name.
     **/
    String AUTHENTICATION_METHOD_ATTRIBUTE = "authenticationMethod";
    /**
     * Tenant ID attribute.
     */
    String TENANT_ID_ATTRIBUTE = "tenant";

    /**
     * Authentication session timeout attribute
     * whose value in seconds would control the
     * session timeout for this authentication event.
     */
    String AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE = "authenticationSessionTimeout";

    /**
     * Authentication date attribute name.
     **/
    String AUTHENTICATION_DATE_ATTRIBUTE = "authenticationDate";

    /**
     * Authenticates the provided credentials. On success, an {@link Authentication} object
     * is returned containing metadata about the result of each authenticated credential.
     * Note that a particular implementation may require some or all credentials to be
     * successfully authenticated. Failure to authenticate is considered an exceptional case, and
     * an AuthenticationException is thrown.
     *
     * @param authenticationTransaction Process a single authentication transaction
     * @return Authentication object on success that contains metadata about credentials that were authenticated.
     * @throws Throwable the throwable
     */
    Authentication authenticate(AuthenticationTransaction authenticationTransaction) throws Throwable;
}
