package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.NamedObject;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                                                            @Nullable RegisteredService registeredService,
                                                            HttpServletRequest httpServletRequest,
                                                            HttpServletResponse httpServletResponse,
                                                            @Nullable Service service) throws Throwable;

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
                             @Nullable final RegisteredService registeredService,
                             final Authentication authentication,
                             @Nullable final Service service) {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
