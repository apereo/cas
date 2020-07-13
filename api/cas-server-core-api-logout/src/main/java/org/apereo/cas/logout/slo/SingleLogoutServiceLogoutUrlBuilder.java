package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link SingleLogoutServiceLogoutUrlBuilder}, which determines
 * which given endpoint of a registered service must receive logout messages.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SingleLogoutServiceLogoutUrlBuilder extends Ordered {

    /**
     * Determine logout url collection.
     *
     * @param registeredService   the registered service
     * @param singleLogoutService the single logout service
     * @return the collection
     */
    default Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                           final WebApplicationService singleLogoutService) {
        return determineLogoutUrl(registeredService, singleLogoutService, Optional.empty());
    }

    /**
     * Determine logout url.
     *
     * @param registeredService   the registered service
     * @param singleLogoutService the single logout service
     * @param httpRequest         the http request
     * @return the URL
     */
    default Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                           final WebApplicationService singleLogoutService,
                                                           final Optional<HttpServletRequest> httpRequest) {
        return new ArrayList<>(0);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getName();
    }

    /**
     * Supports boolean.
     *
     * @param registeredService   the registered service
     * @param singleLogoutService the single logout service
     * @param httpRequest         the http request
     * @return the boolean
     */
    boolean supports(RegisteredService registeredService,
                     WebApplicationService singleLogoutService,
                     Optional<HttpServletRequest> httpRequest);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Is service authorized?
     *
     * @param service the service
     * @param request the request
     * @return true/false
     */
    boolean isServiceAuthorized(WebApplicationService service,
                                Optional<HttpServletRequest> request);
}
