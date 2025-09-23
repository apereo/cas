package org.apereo.cas.ticket;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;

import org.springframework.core.Ordered;

/**
 * This is {@link ServiceTicketGeneratorAuthority}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface ServiceTicketGeneratorAuthority extends Ordered {

    /**
     * Allow service ticket generator authority for all requests.
     *
     * @return the service ticket generator authority
     */
    static ServiceTicketGeneratorAuthority allow() {
        return new ServiceTicketGeneratorAuthority() {
        };
    }

    /**
     * Supports this authentication attempt and service to allow for service ticket generation.
     *
     * @param authenticationResult the authentication result
     * @param service              the service
     * @return true/false
     */
    default boolean supports(final AuthenticationResult authenticationResult, final Service service) {
        return true;
    }

    /**
     * Determine if this authority should allow for service ticket generation.
     * The authority must have already declared support for the request.
     *
     * @param authenticationResult the authentication result
     * @param service              the service
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean shouldGenerate(final AuthenticationResult authenticationResult, final Service service) throws Throwable {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
