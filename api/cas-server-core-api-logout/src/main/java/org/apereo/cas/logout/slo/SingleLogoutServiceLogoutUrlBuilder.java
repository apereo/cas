package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import java.util.Collection;

/**
 * This is {@link SingleLogoutServiceLogoutUrlBuilder}, which determines
 * which given endpoint of a registered service must receive logout messages.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SingleLogoutServiceLogoutUrlBuilder {

    /**
     * Determine logout url.
     *
     * @param registeredService   the registered service
     * @param singleLogoutService the single logout service
     * @return the URL
     */
    Collection<SingleLogoutUrl> determineLogoutUrl(RegisteredService registeredService, WebApplicationService singleLogoutService);
}
