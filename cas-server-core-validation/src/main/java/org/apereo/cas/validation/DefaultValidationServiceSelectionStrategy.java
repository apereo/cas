package org.apereo.cas.validation;

import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link DefaultValidationServiceSelectionStrategy} which returns back to the caller
 * the provided service, as it was without any additional processing. 
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultValidationServiceSelectionStrategy implements ValidationServiceSelectionStrategy {
    
    @Override
    public Service resolveServiceFrom(final Service service) {
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        return true;
    }

    @Override
    public int compareTo(final ValidationServiceSelectionStrategy o) {
        return Integer.MAX_VALUE;
    }
}
