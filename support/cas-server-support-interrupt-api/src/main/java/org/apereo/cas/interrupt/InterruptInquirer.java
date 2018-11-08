package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface InterruptInquirer extends Ordered {

    /**
     * Inquire interrupt response.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @param credential        the credential
     * @param requestContext    the request context
     * @return the interrupt response
     */
    InterruptResponse inquire(Authentication authentication, RegisteredService registeredService,
                              Service service, Credential credential, RequestContext requestContext);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
