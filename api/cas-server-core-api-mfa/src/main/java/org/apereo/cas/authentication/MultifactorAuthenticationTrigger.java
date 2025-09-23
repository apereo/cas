package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.NamedObject;

import org.springframework.core.Ordered;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrigger extends Ordered, NamedObject {

    /**
     * Is activated optional.
     *
     * @param authentication      the authentication
     * @param registeredService   the service
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @param service             the service
     * @return the optional
     * @throws Throwable the throwable
     */
    Optional<MultifactorAuthenticationProvider> isActivated(Authentication authentication,
                                                            RegisteredService registeredService,
                                                            HttpServletRequest httpServletRequest,
                                                            HttpServletResponse httpServletResponse,
                                                            Service service) throws Throwable;

    /**
     * Supports.
     *
     * @param request           the request
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @param service           the service
     * @return true/false
     */
    default boolean supports(final HttpServletRequest request,
                             final RegisteredService registeredService,
                             final Authentication authentication,
                             final Service service) {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
