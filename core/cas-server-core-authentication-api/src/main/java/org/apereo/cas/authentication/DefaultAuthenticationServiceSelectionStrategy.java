package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;

import java.io.Serial;

/**
 * This is {@link DefaultAuthenticationServiceSelectionStrategy} which returns back to the caller
 * the provided service, as it was without any additional processing.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Setter
@Getter
public class DefaultAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {

    @Serial
    private static final long serialVersionUID = -7458940344679793681L;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Service resolveServiceFrom(final Service service) {
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        return true;
    }

}
