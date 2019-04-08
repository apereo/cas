package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrigger extends Ordered {

    /**
     * Is activated optional.
     *
     * @param authentication     the authentication
     * @param registeredService  the service
     * @param httpServletRequest the http servlet request
     * @param service            the service
     * @return the optional
     */
    Optional<MultifactorAuthenticationProvider> isActivated(Authentication authentication,
                                                            RegisteredService registeredService,
                                                            HttpServletRequest httpServletRequest,
                                                            Service service);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
