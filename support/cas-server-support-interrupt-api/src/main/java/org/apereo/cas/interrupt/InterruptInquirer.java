package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface InterruptInquirer extends Ordered, NamedObject {

    /**
     * The bean name of the interrupt inquirer.
     */
    String BEAN_NAME = "interruptInquirer";

    /**
     * Inquire interrupt response.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @param credential        the credential
     * @param requestContext    the request context
     * @return the interrupt response
     * @throws Throwable the throwable
     */
    InterruptResponse inquire(Authentication authentication, RegisteredService registeredService,
                              Service service, Credential credential, RequestContext requestContext) throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
