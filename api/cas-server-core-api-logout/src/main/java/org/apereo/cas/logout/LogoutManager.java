package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutRequestContext;

import java.util.List;

/**
 * A logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@FunctionalInterface
public interface LogoutManager {

    /**
     * Default bean name.
     */
    String DEFAULT_BEAN_NAME = "logoutManager";

    /**
     * Perform a back channel logout for a given ticket granting
     * ticket and returns all the logout requests.
     *
     * @param context the context
     * @return all logout requests.
     */
    List<SingleLogoutRequestContext> performLogout(SingleLogoutExecutionRequest context);
}
