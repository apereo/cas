package org.apereo.cas.adaptors.radius;

import java.io.Serializable;
import java.util.Optional;

/**
 * Interface representing a Radius Server.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@FunctionalInterface
public interface RadiusServer extends Serializable {

    /**
     * The default port for accounting.
     *
     * @since 4.1.0
     */
    int DEFAULT_PORT_ACCOUNTING = 1813;

    /**
     * The default port for authentication.
     *
     * @since 4.1.0
     */
    int DEFAULT_PORT_AUTHENTICATION = 1812;

    /**
     * Method to authenticate a set of credentials.
     *
     * @param username Non-null username to authenticate.
     * @param password Password to authenticate.
     * @return {@link CasRadiusResponse} on success, null otherwise.
     * @throws Exception On indeterminate case where authentication was prevented by a system (e.g. IO) error.
     */
    default CasRadiusResponse authenticate(final String username, final String password) throws Exception {
        return authenticate(username, password, Optional.empty());
    }

    /**
     * Authenticate radius response.
     *
     * @param username the username
     * @param password the password
     * @param state    the state
     * @return the radius response
     * @throws Exception the exception
     */
    CasRadiusResponse authenticate(String username, String password, Optional state) throws Exception;

}
