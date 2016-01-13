package org.jasig.cas.logout;

import org.jasig.cas.services.RegisteredService;

import java.net.URL;

/**
 * This is {@link SingleLogoutServiceLogoutUrlBuilder}, which determines
 * which given endpoint of a registered service must receive logout messages.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface SingleLogoutServiceLogoutUrlBuilder {

    /**
     * Determine logout url.
     *
     * @param registeredService the registered service
     * @param singleLogoutService the single logout service
     * @return the URL
     */
    URL determineLogoutUrl(RegisteredService registeredService, SingleLogoutService singleLogoutService);
}
