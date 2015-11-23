package org.jasig.cas.adaptors.radius;

import org.jasig.cas.authentication.PreventedException;

/**
 * Interface representing a Radius Server.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public interface RadiusServer {
    
    /** The default port for accounting.
     * @since 4.1.0
     **/
    int DEFAULT_PORT_ACCOUNTING = 1813;
    
    /** The default port for authentication.
     * @since 4.1.0
     **/
    int DEFAULT_PORT_AUTHENTICATION = 1812;
    
    /**
     * Method to authenticate a set of credentials.
     *
     * @param username Non-null username to authenticate.
     * @param password Password to authenticate.
     *
     * @return {@link RadiusResponse} on success, null otherwise.
     *
     * @throws PreventedException On indeterminate case where authentication was prevented by a system (e.g. IO) error.
     */
    RadiusResponse authenticate(String username, String password) throws PreventedException;

}
