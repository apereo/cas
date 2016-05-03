package org.jasig.cas.validation;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultValidationServiceSelectionStrategy} which returns back to the caller
 * the provided service, as it was without any additional processing. 
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("defaultValidationServiceSelectionStrategy")
public class DefaultValidationServiceSelectionStrategy implements ValidationServiceSelectionStrategy {

    private static final long serialVersionUID = -1520729797514798602L;

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
        return MAX_ORDER;
    }
}
