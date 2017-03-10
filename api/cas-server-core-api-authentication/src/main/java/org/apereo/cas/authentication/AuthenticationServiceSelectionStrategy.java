package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * This is {@link AuthenticationServiceSelectionStrategy} which attempts to
 * resolve and nominate a service during a validation event. By default
 * most services provided to CAS are taken and resolved verbatim. However,
 * in scenarios where a given module ends up inserting target service identifiers
 * into the service parameter as query strings where the final service URL is
 * designated to be a callback url to CAS which carries the real service identifier,
 * (such as SAML2 support where callbacks are used to route the request back to CAS),
 * an implementation of this strategy may be used to detect the query parameters
 * inside the URL to use the real service for user attribute processing and more.
 * Services resolved via this strategy must still be vetted against the service registry.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface AuthenticationServiceSelectionStrategy extends Serializable, Ordered {
        
    /**
     * Resolves the real service from the provided service, if appropriate.
     *
     * @param service the provided service by the caller
     * @return the resolved service
     */
    Service resolveServiceFrom(Service service);

    /**
     * Indicates whether this strategy supports service selection.
     *
     * @param service the service
     * @return true/false
     */
    boolean supports(Service service);
}
