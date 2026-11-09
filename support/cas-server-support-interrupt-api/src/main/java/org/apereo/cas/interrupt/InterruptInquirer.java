package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

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
     * Inquire interrupt response. Inquirers do not depend on the login webflow, so they can also be asked outside of
     * it (for example by the interrupts actuator endpoint); an inquirer that needs the current HTTP request reads it
     * from the request attributes of the current thread, if any.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param service           the service
     * @param credential        the credential
     * @return the interrupt response
     * @throws Throwable the throwable
     */
    InterruptResponse inquire(Authentication authentication, RegisteredService registeredService,
                              Service service, Credential credential)
            throws Throwable;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
