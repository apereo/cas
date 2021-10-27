package org.jasig.cas.logout;

import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Define a service which support single logout.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public interface SingleLogoutService extends WebApplicationService {

    /**
     * Return if the service is already logged out.
     *
     * @return if the service is already logged out.
     */
    boolean isLoggedOutAlready();

    /**
     * Set if the service is already logged out.
     *
     * @param loggedOutAlready if the service is already logged out.
     */
    void setLoggedOutAlready(boolean loggedOutAlready);
}
