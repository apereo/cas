package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;

/**
 * This is {@link DefaultAuthenticationRequestServiceSelectionStrategy} which returns back to the caller
 * the provided service, as it was without any additional processing. 
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultAuthenticationRequestServiceSelectionStrategy implements AuthenticationRequestServiceSelectionStrategy {

    private static final long serialVersionUID = -7458940344679793681L;

    @Override
    public Service resolveServiceFrom(final Service service) {
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        return true;
    }

    @Override
    public int compareTo(final AuthenticationRequestServiceSelectionStrategy o) {
        return Integer.MAX_VALUE;
    }
}
